/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;

/**
 * @author Akshay Srivastava
 */
public class WorkflowDetailsRequest {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDetailsRequest.class);

    private static final String namespace = "http://www.informatica.com/wsh";

    private SOAPBody soapBody;
    private String folderName;
    private String workflowName;
    private String serverName;
    private String domainName;

    public WorkflowDetailsRequest(SOAPBody soapBody, String folderName, String workflowName, String serverName, String domainName) {
        this.soapBody = soapBody;
        this.folderName = folderName;
        this.workflowName = workflowName;
        this.serverName = serverName;
        this.domainName = domainName;
    }

    public SOAPBody generateSoapBody(){
        try {
            SOAPElement workflowDetails = soapBody.addChildElement("ns0:GetWorkflowDetails");
            SOAPElement DIServiceInfo = workflowDetails.addChildElement("DIServiceInfo");
            SOAPElement domainElement = DIServiceInfo.addChildElement("DomainName");
            SOAPElement serviceElement = DIServiceInfo.addChildElement("ServiceName");
            SOAPElement folderElement = workflowDetails.addChildElement("FolderName");
            SOAPElement workflowElement = workflowDetails.addChildElement("WorkflowName");
            domainElement.addTextNode(domainName.toString());
            serviceElement.addTextNode(serverName.toString());
            folderElement.addTextNode(folderName.toString());
            workflowElement.addTextNode(workflowName.toString());

            soapBody.addNamespaceDeclaration("ns0" , namespace);
        }catch(Exception e){
            logger.error("Error generating workflowDetail request body: ", e);
        }
        return soapBody;
    }
}
