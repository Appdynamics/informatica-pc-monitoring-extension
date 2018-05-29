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
import com.appdynamics.monitors.informatica.dto.DIServerInfo;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import com.appdynamics.monitors.informatica.response.AllFoldersResponse;
import com.appdynamics.monitors.informatica.saop.SOAPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPMessage;
import java.util.List;
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

    private SOAPClient soapClient;

    private static String sessionID;

    private List<DIServerInfo> diServerInfos;

    public FoldersTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser, SOAPClient soapClient, String sessionID, List<DIServerInfo> diServerInfo) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.phaser = phaser;
        this.soapClient = soapClient;
        this.sessionID = sessionID;
        this.diServerInfos = diServerInfo;
        phaser.register();
    }

    /**
     * Fetches getAllFolders response, then for each folder creates a new thread of AllWorkflowTask
     */
    public void run() {
        try {
            SOAPMessage soapResponse = soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.GETALLFOLDERS.name(), instance, sessionID, null, null, null);

            AllFoldersResponse allFoldersResponse = new AllFoldersResponse(soapResponse);

            //Having retrieved allFolders, get all work-flows for each folder one by one with max 2 attempts
            for(String folderName : allFoldersResponse.getFoldersInfo()){
                logger.debug("Creating getAllWorkflows Task");
                AllWorkFlowsTask allWorkFlowsTask = new AllWorkFlowsTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, soapClient, sessionID, folderName, diServerInfos);
                contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", allWorkFlowsTask);
            }
            phaser.arriveAndAwaitAdvance();

        }catch(Exception e){
            logger.error("FoldersTask task error: ", e);
        }finally {
            logger.debug("FoldersTask Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }
}
