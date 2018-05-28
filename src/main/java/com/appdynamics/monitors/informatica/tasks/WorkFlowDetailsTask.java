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

public class WorkFlowDetailsTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowDetailsTask.class);

    private MonitorContextConfiguration contextConfiguration;

    private MetricWriteHelper metricWriteHelper;

    private Instance instance;

    private String metricPrefix;

    private Phaser phaser;

    private SOAPClient soapClient;

    private String sessionID;

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

    public void run() {
        try {
            logger.debug("Creating WorkFlowDetails request");
            SOAPMessage soapResponse = soapClient.callSoapWebService(instance.getHost() + "DataIntegration", RequestTypeEnum.GETWORKFLOWDETAILS.name(), instance, sessionID, folderName, workflowName, diServerInfoList.get(0).getServiceName());


            /*String mssg =
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "   <soapenv:Header>" +
                            "      <ns1:Context xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                            "         <SessionId>cd032b685b50477e16321983a5a</SessionId>" +
                            "      </ns1:Context>" +
                            "   </soapenv:Header>" +
                            "   <soapenv:Body>" +
                            "      <ns1:GetWorkflowDetailsReturn xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                            "         <FolderName>BI_CMNSTG</FolderName>" +
                            "         <WorkflowName>wf_CMNSTG_ET</WorkflowName>" +
                            "         <WorkflowRunId>15905150</WorkflowRunId>" +
                            "         <WorkflowRunInstanceName/>" +
                            "         <WorkflowRunStatus>FAILED</WorkflowRunStatus>" +
                            "         <WorkflowRunType>SCHEDULE</WorkflowRunType>" +
                            "         <RunErrorCode>36331</RunErrorCode>" +
                            "         <RunErrorMessage>WARNING: Worklet task instance [PASS1] failed and its \"fail parent if this task fails\" setting is turned on.  So, Workflow [wf_CMNSTG_ET] will be failed.</RunErrorMessage>" +
                            "         <StartTime>" +
                            "            <Date>1</Date>" +
                            "            <NanoSeconds>0</NanoSeconds>" +
                            "            <Seconds>1</Seconds>" +
                            "            <Minutes>35</Minutes>" +
                            "            <Hours>20</Hours>" +
                            "            <Month>5</Month>" +
                            "            <Year>2018</Year>" +
                            "            <UTCTime>1525224901</UTCTime>" +
                            "         </StartTime>" +
                            "         <EndTime>" +
                            "            <Date>1</Date>" +
                            "            <NanoSeconds>0</NanoSeconds>" +
                            "            <Seconds>58</Seconds>" +
                            "            <Minutes>13</Minutes>" +
                            "            <Hours>21</Hours>" +
                            "            <Month>5</Month>" +
                            "            <Year>2018</Year>" +
                            "            <UTCTime>1525227238</UTCTime>" +
                            "         </EndTime>" +
                            "         <UserName>Administrator</UserName>" +
                            "         <LogFileName>F:\\nagp_infa_cinta\\NAGP_IS_ASCII\\WorkflowLogs\\wf_CMNSTG_ET.log</LogFileName>" +
                            "         <LogFileCodePage>2252</LogFileCodePage>" +
                            "         <OSUser/>" +
                            "      </ns1:GetWorkflowDetailsReturn>" +
                            "   </soapenv:Body>" +
                            "</soapenv:Envelope>";
            InputStream is = new ByteArrayInputStream(mssg.getBytes());
            SOAPMessage responseStr = MessageFactory.newInstance().createMessage(null, is);
*/

            WorkflowInfo workflowInfo = new WorkflowInfo(folderName, workflowName);
            WorkflowDetailsResponse workflowDetailsResponse = new WorkflowDetailsResponse(soapResponse, workflowInfo);
            WorkflowInfo workflowDetails = workflowDetailsResponse.getWorkflowDetails();

            //List<Map> metricList = (List<Map>) contextConfiguration.getConfigYml().get("metrics");

            metrics.add(new Metric("WorkflowRunStatus", Integer.toString(workflowDetails.getWorkflowRunStatus().ordinal()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|"));
            metrics.add(new Metric("WorkflowRunType", Integer.toString(workflowDetails.getWorkflowRunType().ordinal()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|"));
            metrics.add(new Metric("RunErrorCode", Long.toString(workflowDetails.getRunErrorCode()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|"));

            metrics.add(new Metric("WorkflowRunId", Long.toString(workflowDetails.getWorkflowRunID()), metricPrefix + "|"
                    + workflowDetails.getFolderName() + "|" + workflowDetails.getName() + "|"));


            if (metrics != null && metrics.size() > 0) {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }

    }catch(Exception e){
            logger.error("WorkFlowDetailsTask task error: " + e.getMessage());
        }finally {
            logger.debug("WorkFlowDetailsTask Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }
}
