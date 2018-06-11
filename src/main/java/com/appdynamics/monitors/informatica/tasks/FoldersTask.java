/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.tasks;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.metadata.DIServerInfoArray;
import com.appdynamics.monitors.informatica.metadata.FolderInfo;
import com.appdynamics.monitors.informatica.metadata.FolderInfoArray;
import com.appdynamics.monitors.informatica.metadata.MetadataInterface;
import com.appdynamics.monitors.informatica.metadata.MetadataService;
import com.appdynamics.monitors.informatica.metadata.VoidRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.Phaser;

/**
 * @author Akshay Srivastava
 */
public class FoldersTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FoldersTask.class);

    private MonitorContextConfiguration contextConfiguration;

    private MetricWriteHelper metricWriterHelper;

    private Instance instance;

    private String metricPrefix;

    private Phaser phaser;

    private URL metadataURL;

    private URL dataIntegrationURL;

    private DIServerInfoArray diServerInfos;

    public FoldersTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser, DIServerInfoArray diServerInfo, URL metadataURL, URL dataIntegrationURL) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.diServerInfos = diServerInfo;
        this.metadataURL = metadataURL;
        this.dataIntegrationURL = dataIntegrationURL;
        phaser.register();
        this.phaser = phaser;
    }

    /**
     * Fetches getAllFolders response, then for each folder creates a new thread of AllWorkflowTask
     */
    public void run() {
        try {
            MetadataService service = new MetadataService(metadataURL);

            MetadataInterface server = service.getMetadata();

            VoidRequest allFoldersReq = new VoidRequest();

            //Retrieve allDIServers, ping each server one by one with max 2 attempts
            FolderInfoArray allFoldersResponse = server.getAllFolders(allFoldersReq);
            logger.debug("All folders response returned folder list of size: " + allFoldersResponse.getFolderInfo().size());

            //Having retrieved allFolders, get all work-flows for each folder one by one with max 2 attempts
            for (FolderInfo folderInfo : allFoldersResponse.getFolderInfo()) {
                logger.debug("Creating getAllWorkflows Task");
                AllWorkFlowsTask allWorkFlowsTask = new AllWorkFlowsTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, folderInfo, diServerInfos, metadataURL, dataIntegrationURL);
                contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", allWorkFlowsTask);
            }
            phaser.arriveAndAwaitAdvance();

        } catch (Exception e) {
            logger.error("FoldersTask task error: ", e);
        } finally {
            logger.debug("FoldersTask Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }
}
