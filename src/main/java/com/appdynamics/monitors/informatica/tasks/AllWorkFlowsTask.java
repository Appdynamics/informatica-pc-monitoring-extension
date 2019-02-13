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
import com.appdynamics.monitors.informatica.metadata.MetadataInterface;
import com.appdynamics.monitors.informatica.metadata.MetadataService;
import com.appdynamics.monitors.informatica.metadata.WorkflowInfo;
import com.appdynamics.monitors.informatica.metadata.WorkflowInfoArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.Phaser;

/**
 * @author Akshay Srivastava
 */
public class AllWorkFlowsTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AllWorkFlowsTask.class);

    private MonitorContextConfiguration contextConfiguration;

    private MetricWriteHelper metricWriterHelper;

    private Instance instance;

    private String metricPrefix;

    private Phaser phaser;

    private FolderInfo folderInfo;

    private URL metadataURL;

    private URL dataIntegrationURL;


    private DIServerInfoArray DIServerInfos;

    public AllWorkFlowsTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriterHelper, String metricPrefix, Phaser phaser, FolderInfo folderInfo, DIServerInfoArray DIServerInfos, URL metadataURL, URL dataIntegrationURL) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriterHelper;
        this.metricPrefix = metricPrefix;
        this.folderInfo = folderInfo;
        this.DIServerInfos = DIServerInfos;
        this.metadataURL = metadataURL;
        this.dataIntegrationURL = dataIntegrationURL;
        phaser.register();
        this.phaser = phaser;
    }

    /**
     * Fetches allWorkflow response, then for each workflow creates a new thread of getWorkflowDetails.
     */
    public void run() {
        try {
            logger.debug("Creating workflowDetails request");

            MetadataService service = new MetadataService(metadataURL);

            MetadataInterface server = service.getMetadata();

            WorkflowInfoArray allWorkflowResponse = server.getAllWorkflows(folderInfo);


            //Having retrieved workflows, get details for each workflow one by one with max 2 attempts( one for each server present in DIServerInfos)
            for (WorkflowInfo workflowInfo : allWorkflowResponse.getWorkflowInfo()) {

                logger.debug("Creating workflowDetails Task");
                WorkFlowDetailsTask workFlowDetailsTask = new WorkFlowDetailsTask(instance, metricWriterHelper, metricPrefix, phaser, folderInfo, workflowInfo.getName(), DIServerInfos, dataIntegrationURL);
                contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", workFlowDetailsTask);
            }
            phaser.arriveAndAwaitAdvance();
        } catch (Exception e) {
            logger.error("WorkflowDetail task error: ", e);
        } finally {
            logger.debug("Workflow Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }
}
