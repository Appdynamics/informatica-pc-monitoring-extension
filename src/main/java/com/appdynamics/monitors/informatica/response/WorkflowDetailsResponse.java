/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.response;

import com.appdynamics.monitors.informatica.dto.WorkflowInfo;
import com.appdynamics.monitors.informatica.enums.WorkflowRunStatusEnum;
import com.appdynamics.monitors.informatica.enums.WorkflowRunTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class WorkflowDetailsResponse {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDetailsResponse.class);

    private SOAPMessage soapResponse;

    public WorkflowDetailsResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    public WorkflowInfo getWorkflowDetails(){

        WorkflowInfo workflowInfo = new WorkflowInfo();
        try {
            SOAPBody body = soapResponse.getSOAPBody();
            NodeList list = body.getElementsByTagName("ns1:GetWorkflowDetailsReturn");

            for (int i = 0; i < list.getLength(); i++) {
                NodeList innerList = list.item(i).getChildNodes();

                for (int j = 0; j < innerList.getLength(); j++) {
                    if(innerList.item(j).getNodeName().equals("FolderName")){
                        workflowInfo.setFolderName(innerList.item(j).getTextContent());
                    }
                    if(innerList.item(j).getNodeName().equals("WorkflowName")){
                        workflowInfo.setName(innerList.item(j).getTextContent());
                    }
                    if(innerList.item(j).getNodeName().equals("WorkflowRunStatus")){
                        workflowInfo.setWorkflowRunStatus(WorkflowRunStatusEnum.valueOf(innerList.item(j).getTextContent()));
                    }
                    if(innerList.item(j).getNodeName().equals("WorkflowRunType")){
                        workflowInfo.setWorkflowRunType(WorkflowRunTypeEnum.valueOf(innerList.item(j).getTextContent()));
                    }
                    if(innerList.item(j).getNodeName().equals("RunErrorCode")){
                        workflowInfo.setRunErrorCode(Long.valueOf(innerList.item(j).getTextContent()));
                    }
                    if(innerList.item(j).getNodeName().equals("WorkflowRunId")){
                        workflowInfo.setWorkflowRunID(Long.valueOf(innerList.item(j).getTextContent()));
                    }
                }
            }

        }catch(SOAPException e){
            logger.error("SOAPException retrieving workflowDetails response: ", e);
        }
        return workflowInfo;
    }
}
