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
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.dto.DIServerInfo;
import com.appdynamics.monitors.informatica.dto.WorkflowInfo;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import com.appdynamics.monitors.informatica.response.WorkflowDetailsResponse;
import com.appdynamics.monitors.informatica.saop.SOAPClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * @author Akshay Srivastava
 */
public class WorkFlowDetailsTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowDetailsTask.class);

    private MonitorContextConfiguration contextConfiguration;

    private MetricWriteHelper metricWriteHelper;

    private Instance instance;

    private String metricPrefix;

    private Phaser phaser;

    private SOAPClient soapClient;

    private static String sessionID;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String folderName;

    private String workflowName;

    private List<DIServerInfo> diServerInfoList;

    public WorkFlowDetailsTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser, SOAPClient soapClient, String sessionID, String folderName, String workflowName, List<DIServerInfo> diServerInfoList) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.phaser = phaser;
        this.soapClient = soapClient;
        this.sessionID = sessionID;
        this.folderName = folderName;
        this.workflowName = workflowName;
        this.diServerInfoList = diServerInfoList;
        phaser.register();
    }

    /**
     * Fetches and publishes workflowDetails metrics
     */
    public void run() {
        try {

            phaser.arriveAndAwaitAdvance();
            logger.debug("Creating WorkFlowDetails request");
            //TODO: Add logic for checking second server in case first one fails: after error message sample resopnse is provided by client
            SOAPMessage soapResponse = soapClient.callSoapWebService(instance.getHost() + "DataIntegration", RequestTypeEnum.GETWORKFLOWDETAILS.name(), instance, sessionID, folderName, workflowName, diServerInfoList.get(0).getServiceName());

            WorkflowDetailsResponse workflowDetailsResponse = new WorkflowDetailsResponse(soapResponse);
            WorkflowInfo workflowDetails = workflowDetailsResponse.getWorkflowDetails();

            //List<Map> metricList = (List<Map>) contextConfiguration.getConfigYml().get("metrics");

            metrics.add(new Metric("WorkflowRunStatus", Integer.toString(workflowDetails.getWorkflowRunStatus().ordinal()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|" + "WorkflowRunStatus"));
            metrics.add(new Metric("WorkflowRunType", Integer.toString(workflowDetails.getWorkflowRunType().ordinal()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|" + "WorkflowRunType"));
            metrics.add(new Metric("RunErrorCode", Long.toString(workflowDetails.getRunErrorCode()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|" + "RunErrorCode"));
            metrics.add(new Metric("WorkflowRunId", Long.toString(workflowDetails.getWorkflowRunID()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|" + "WorkflowRunId"));

            if (metrics != null && metrics.size() > 0) {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }

        } catch (Exception e) {
            logger.error("WorkFlowDetailsTask task error: ", e);
        } finally {
            logger.debug("WorkFlowDetailsTask Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
            if (sessionID != null) {
                soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.LOGOUT.name(), instance, sessionID, null, null, null);
            }
        }
    }
}
