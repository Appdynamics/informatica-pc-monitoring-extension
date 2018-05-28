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
public class AllWorkFlowsRequest {

    private static final Logger logger = LoggerFactory.getLogger(AllWorkFlowsRequest.class);

    private static final String namespace = "http://www.informatica.com/wsh";

    private SOAPBody soapBody;
    private String folderName;

    public AllWorkFlowsRequest(SOAPBody soapBody, String folderName) {
        this.soapBody = soapBody;
        this.folderName = folderName;
    }

    public SOAPBody generateSoapBody(){
        try {
            SOAPElement getAllWorkFlows = soapBody.addChildElement("ns0: GetAllWorkflows");
            SOAPElement name = getAllWorkFlows.addChildElement("Name");
            name.addTextNode(folderName.toString());

            soapBody.addNamespaceDeclaration("ns0" , namespace);

        }catch(Exception e){
            logger.error("Error generating allWorkflows request body: " + e.getStackTrace());
        }
        return soapBody;
    }
}
