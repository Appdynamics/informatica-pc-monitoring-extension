/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.request;


import com.appdynamics.monitors.informatica.dto.DIServerInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import java.util.Map;

/**
 * @author Akshay Srivastava
 */
public class PingDIServerRequest {

   private static final Logger logger = LoggerFactory.getLogger(PingDIServerRequest.class);

   private static final String namespace = "http://www.informatica.com/wsh";

   private SOAPBody soapBody;
   private String serverName;
   private String domainName;

    public PingDIServerRequest(SOAPBody soapBody, String domainName, String serverName) {
        this.soapBody = soapBody;
        this.serverName = serverName;
        this.domainName = domainName;
    }

    public SOAPBody generateSoapBody(){
        try {
            SOAPElement pingDIServer = soapBody.addChildElement("ns0: PingDIServer");
            SOAPElement DIServiceInfo = pingDIServer.addChildElement("DIServiceInfo");
            SOAPElement domainElement = DIServiceInfo.addChildElement("DomainName");
            SOAPElement serviceElement = DIServiceInfo.addChildElement("ServiceName");
            domainElement.addTextNode(domainName.toString());
            serviceElement.addTextNode(serverName.toString());

            soapBody.addNamespaceDeclaration("ns0" , namespace);

        }catch(Exception e){
            logger.error("Error generating pingDIServer request body: ", e);
        }
        return soapBody;
    }
}
