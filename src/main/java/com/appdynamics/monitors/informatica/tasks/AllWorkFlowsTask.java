/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.tasks;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.dto.DIServerInfo;
import com.appdynamics.monitors.informatica.dto.WorkflowInfo;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import com.appdynamics.monitors.informatica.response.AllWorkflowResponse;
import com.appdynamics.monitors.informatica.saop.SOAPClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private SOAPClient soapClient;

    private static String sessionID;

    private String folderName;

    private List<DIServerInfo> DIServerInfos;

    public AllWorkFlowsTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriterHelper, String metricPrefix, Phaser phaser, SOAPClient soapClient, String sessionID, String folderName, List<DIServerInfo> DIServerInfos) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriterHelper;
        this.metricPrefix = metricPrefix;
        this.phaser = phaser;
        this.soapClient = soapClient;
        this.sessionID = sessionID;
        this.folderName = folderName;
        this.DIServerInfos = DIServerInfos;
        phaser.register();
    }

    /**
     * Fetches allWorkflow response, then for each workflow creates a new thread of getWorkflowDetails.
     */
    public void run() {
        try {
            logger.debug("Creating workflowDetails request");
            SOAPMessage soapResponse = soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.GETALLWORKFLOWS.name(), instance, sessionID, folderName, null, null);

            AllWorkflowResponse allWorkflowResponse = new AllWorkflowResponse(soapResponse);
            List<WorkflowInfo> workflowList = allWorkflowResponse.getAllWorkflows();

            //Having retrieved workflows, get details for each workflow one by one with max 2 attempts( one for each server present in DIServerInfos)
            for(WorkflowInfo workflowInfo : workflowList){

                logger.debug("Creating workflowDetails Task");
                WorkFlowDetailsTask workFlowDetailsTask = new WorkFlowDetailsTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, soapClient, sessionID, folderName, workflowInfo.getName(), DIServerInfos);
                contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", workFlowDetailsTask);
            }
            phaser.arriveAndAwaitAdvance();
        }catch(Exception e){
            logger.error("WorkflowDetail task error: ", e);
        }finally {
            logger.debug("Workflow Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }
}
