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

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class WorkflowDetailsResponse {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDetailsResponse.class);

    private SOAPMessage soapResponse;
    private WorkflowInfo workflowInfo;

    public WorkflowDetailsResponse(SOAPMessage soapResponse, WorkflowInfo workflowInfo) {
        this.soapResponse = soapResponse;
        this.workflowInfo = workflowInfo;
    }

    public WorkflowInfo getWorkflowDetails(){

        try {
            SOAPBody body = soapResponse.getSOAPBody();
            workflowInfo.setWorkflowRunStatus(WorkflowRunStatusEnum.valueOf(body.getAttribute("WorkflowRunStatus")));
            workflowInfo.setWorkflowRunType(WorkflowRunTypeEnum.valueOf(body.getAttribute("WorkflowRunType")));
            workflowInfo.setRunErrorCode(Long.valueOf(body.getAttribute("RunErrorCode")));
            workflowInfo.setWorkflowRunID(Long.valueOf(body.getAttribute("WorkflowRunId")));

        }catch(SOAPException e){
            logger.error("SOAPException retrieving workflowDetails response: " + e.getMessage());
        }
        return workflowInfo;
    }
}
