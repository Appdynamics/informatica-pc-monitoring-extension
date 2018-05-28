/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.response;

import com.appdynamics.monitors.informatica.dto.WorkflowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Akshay Srivastava
 */
public class AllWorkflowResponse {

    private static final Logger logger = LoggerFactory.getLogger(AllWorkflowResponse.class);

    private SOAPMessage soapResponse;

    public AllWorkflowResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    public List<WorkflowInfo> getAllWorkflows(){

        List<WorkflowInfo> workflowInfoList = new ArrayList<>();
        try {
            SOAPBody body = soapResponse.getSOAPBody();
            NodeList list = body.getElementsByTagName("WorkflowInfo");

            for (int i = 0; i < list.getLength(); i++) {
                NodeList innerList = list.item(i).getChildNodes();

                WorkflowInfo workflowInfo = new WorkflowInfo();
                for (int j = 0; j < innerList.getLength(); j++) {
                    if(innerList.item(j).getNodeName().equals("Name")){
                        workflowInfo.setName(innerList.item(j).getTextContent());
                    }
                    if(innerList.item(j).getNodeName().equals("IsValid")){
                        workflowInfo.setValid(Boolean.valueOf(innerList.item(j).getTextContent()));
                    }
                    if(innerList.item(j).getNodeName().equals("FolderName")){
                        workflowInfo.setFolderName(innerList.item(j).getTextContent());
                    }
                    logger.debug("Adding workflow info object : " + workflowInfo.getName() + " : " + workflowInfo.getFolderName() + " to the list");
                }
                workflowInfoList.add(workflowInfo);
            }
        }catch(SOAPException e) {
            logger.error("SOAPException retrieving WorkflowInfo response: " + e.getMessage());
        }
        return workflowInfoList;
    }
}
