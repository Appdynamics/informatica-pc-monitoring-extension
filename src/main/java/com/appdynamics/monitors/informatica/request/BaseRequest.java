/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.request;


import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

/**
 * @author Akshay Srivastava
 */
public class BaseRequest {

    private SOAPEnvelope envelope;

    private static final String namespace = "http://www.informatica.com/wsh";

    public BaseRequest(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }

    public SOAPBody getRequestBody(RequestTypeEnum requestName, Instance instanceInfo, String folderName, String workflowName, String serverName) throws SOAPException{

        SOAPBody soapBody = envelope.getBody();
        switch (requestName){
            case LOGIN:
                LoginRequest loginRequest = new LoginRequest(soapBody, instanceInfo);
                soapBody = loginRequest.generateSoapBody();
                break;
            case ALLDISERVERS:
                soapBody.addChildElement("ns0:GetAllDIServers");
                soapBody.addNamespaceDeclaration("ns0" , namespace);
                break;
            case PINGDISERVER:
                PingDIServerRequest pingDIServer = new PingDIServerRequest(soapBody, instanceInfo.getDomainName(), serverName);
                soapBody = pingDIServer.generateSoapBody();
                break;
            case GETALLFOLDERS:
                soapBody.addChildElement("ns0:GetAllFolders");
                soapBody.addNamespaceDeclaration("ns0" , namespace);
                break;
            case GETALLWORKFLOWS:
                AllWorkFlowsRequest allWorkflowsRequest = new AllWorkFlowsRequest(soapBody, folderName);
                soapBody = allWorkflowsRequest.generateSoapBody();
                break;
            case GETWORKFLOWDETAILS:
                WorkflowDetailsRequest workflowDetailsRequest = new WorkflowDetailsRequest(soapBody, instanceInfo.getDomainName(), folderName, workflowName, serverName);
                soapBody = workflowDetailsRequest.generateSoapBody();
                break;
            case LOGOUT:
                soapBody.addChildElement("ns0:Logout");
                soapBody.addNamespaceDeclaration("ns0" , namespace);
                break;
        }
        return soapBody;
    }
}
