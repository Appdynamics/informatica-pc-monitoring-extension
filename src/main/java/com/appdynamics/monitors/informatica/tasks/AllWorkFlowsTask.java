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

public class AllWorkFlowsTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AllWorkFlowsTask.class);

    private MonitorContextConfiguration contextConfiguration;

    private MetricWriteHelper metricWriterHelper;

    private Instance instance;

    private String metricPrefix;

    private Phaser phaser;

    private SOAPClient soapClient;

    private String sessionID;

    private ObjectMapper objectMapper = new ObjectMapper();

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

    public void run() {
        try {
            logger.debug("Creating workflowDetails request");
            SOAPMessage soapResponse = soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.GETALLWORKFLOWS.name(), instance, sessionID, folderName, null, null);

            /*String mssg =
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "   <soapenv:Header>" +
                            "      <ns1:Context xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                            "         <SessionId>cd032b685b50477e16321983a5a</SessionId>" +
                            "      </ns1:Context>" +
                            "   </soapenv:Header>" +
                            "   <soapenv:Body>" +
                            "      <ns1:GetAllWorkflowsReturn xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                            "         <WorkflowInfo>" +
                            "            <Name>wf_CMNSTG_COMPLIANCE_INSIGHT_1TL</Name>" +
                            "            <IsValid>true</IsValid>" +
                            "            <FolderName>BI_CMNSTG</FolderName>" +
                            "         </WorkflowInfo>" +
                            "         <WorkflowInfo>" +
                            "            <Name>wf_CMNSTG_ET</Name>" +
                            "            <IsValid>true</IsValid>" +
                            "            <FolderName>BI_CMNSTG</FolderName>" +
                            "         </WorkflowInfo>" +
                            "      </ns1:GetAllWorkflowsReturn>" +
                            "   </soapenv:Body>" +
                            "</soapenv:Envelope>";
            InputStream is = new ByteArrayInputStream(mssg.getBytes());
            SOAPMessage responseStr = MessageFactory.newInstance().createMessage(null, is);*/


            AllWorkflowResponse allWorkflowResponse = new AllWorkflowResponse(soapResponse);
            List<WorkflowInfo> workflowList = allWorkflowResponse.getAllWorkflows();

            //Having retrieved workflows, get details for each workflow one by one with max 2 attempts
            for(WorkflowInfo workflowInfo : workflowList){

                logger.debug("Creating workflowDetails Task");
                WorkFlowDetailsTask workFlowDetailsTask = new WorkFlowDetailsTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, soapClient, sessionID, folderName, workflowInfo.getName(), DIServerInfos);
                contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", workFlowDetailsTask);
            }

        }catch(Exception e){
            logger.error("WorkflowDetail task error: " + e.getMessage());
        }finally {
            logger.debug("Workflow Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }
}
