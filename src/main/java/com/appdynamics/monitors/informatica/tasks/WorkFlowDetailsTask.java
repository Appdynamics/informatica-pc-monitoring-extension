/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.tasks;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.informatica.IPMonitorTask;
import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.dataIntegration.DIServiceInfo;
import com.appdynamics.monitors.informatica.dataIntegration.DataIntegrationInterface;
import com.appdynamics.monitors.informatica.dataIntegration.DataIntegrationService;
import com.appdynamics.monitors.informatica.dataIntegration.VoidRequest;
import com.appdynamics.monitors.informatica.dataIntegration.WorkflowDetails;
import com.appdynamics.monitors.informatica.dataIntegration.WorkflowRequest;
import com.appdynamics.monitors.informatica.metadata.DIServerInfoArray;
import com.appdynamics.monitors.informatica.metadata.FolderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;

/**
 * @author Akshay Srivastava
 */
public class WorkFlowDetailsTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowDetailsTask.class);

    private MetricWriteHelper metricWriteHelper;

    private Instance instance;

    private String metricPrefix;

    private Phaser phaser;

    private List<Metric> metrics = new ArrayList<Metric>();

    private FolderInfo folderInfo;

    private String workflowName;

    private URL dataIntegrationURL;

    private DIServerInfoArray diServerInfoList;

    public WorkFlowDetailsTask(Instance instance, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser, FolderInfo folderInfo, String workflowName, DIServerInfoArray diServerInfoList, URL dataIntegrationURL) {
        this.instance = instance;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.folderInfo = folderInfo;
        this.workflowName = workflowName;
        this.diServerInfoList = diServerInfoList;
        this.dataIntegrationURL = dataIntegrationURL;
        phaser.register();
        this.phaser = phaser;
    }

    /**
     * Fetches and publishes workflowDetails metrics
     */
    public void run() {
        try {
            metricPrefix = metricPrefix + instance.getDomainName();
            phaser.arriveAndAwaitAdvance();
            logger.debug("Creating WorkFlowDetails request");

            DataIntegrationService DIservice = new DataIntegrationService(dataIntegrationURL);

            DataIntegrationInterface DIServer = DIservice.getDataIntegration();

            DIServiceInfo diServiceInfo = new DIServiceInfo();
            diServiceInfo.setDomainName(instance.getDomainName());
            diServiceInfo.setServiceName(diServerInfoList.getDIServerInfo().get(0).getName());

            WorkflowRequest workflowRequest = new WorkflowRequest();

            workflowRequest.setDIServiceInfo(diServiceInfo);
            workflowRequest.setFolderName(folderInfo.getName());
            workflowRequest.setWorkflowName(workflowName);

            WorkflowDetails workflowDetails = DIServer.getWorkflowDetails(workflowRequest);

            // In case of error response from one server try the next server,
            /*if(soapResponse.getSOAPBody().hasFault() &&
                    soapResponse.getSOAPBody().getFault().getFaultCode().equals("Client")) {
                logger.debug("Error received fetching workflowDetails from " + diServerInfoList.get(0).getServiceName());
                logger.debug("Attempting to fetch workflowDetails from " + diServerInfoList.get(1).getServiceName());
                soapResponse = SOAPClient.callSoapWebService(instance.getHost() + "DataIntegration", RequestTypeEnum.GETWORKFLOWDETAILS.name(), instance, folderName, workflowName, diServerInfoList.get(1).getServiceName());
            }*/

            metrics.add(new Metric("WorkflowRunStatus", Integer.toString(workflowDetails.getWorkflowRunStatus().ordinal()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getWorkflowName() + "|" + "WorkflowRunStatus"));
            metrics.add(new Metric("WorkflowRunType", Integer.toString(workflowDetails.getWorkflowRunType().ordinal()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getWorkflowName() + "|" + "WorkflowRunType"));
            metrics.add(new Metric("RunErrorCode", Long.toString(workflowDetails.getRunErrorCode()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getWorkflowName() + "|" + "RunErrorCode"));
            metrics.add(new Metric("WorkflowRunId", Long.toString(workflowDetails.getWorkflowRunId()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getWorkflowName() + "|" + "WorkflowRunId"));

            if (metrics != null && metrics.size() > 0) {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }

        } catch (Exception e) {
            logger.error("WorkFlowDetailsTask task error: ", e);
        } finally {
            logger.debug("WorkFlowDetailsTask Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
            if (IPMonitorTask.sessionID != null) {
                try {
                    DataIntegrationService DIservice = new DataIntegrationService(dataIntegrationURL);

                    DataIntegrationInterface DIServer = DIservice.getDataIntegration();

                    DIServer.logout(new VoidRequest());
                } catch (Exception e) {
                    logger.error("Logout exception error: ", e);
                }
            }
        }
    }
}
