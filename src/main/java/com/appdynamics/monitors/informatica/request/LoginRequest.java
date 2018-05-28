/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.request;

import com.appdynamics.monitors.informatica.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;

/**
 * @author Akshay Srivastava
 */
public class LoginRequest {

    private static final Logger logger = LoggerFactory.getLogger(LoginRequest.class);

    private static final String namespace = "http://www.informatica.com/wsh";

    private SOAPBody soapBody;
    private Instance instanceInfo;

    public LoginRequest(SOAPBody soapBody, Instance instanceInfo) {
        this.soapBody = soapBody;
        this.instanceInfo = instanceInfo;
    }


    public SOAPBody generateSoapBody(){

        try {
            SOAPElement login = soapBody.addChildElement("ns0:Login");

            SOAPElement repositoryDomainName = login.addChildElement("RepositoryDomainName");
            repositoryDomainName.addTextNode(instanceInfo.getDomainName());
            SOAPElement repositoryName = login.addChildElement("RepositoryName");
            repositoryName.addTextNode(instanceInfo.getRepositoryName());
            SOAPElement userName = login.addChildElement("UserName");
            userName.addTextNode(instanceInfo.getUsername());
            SOAPElement password = login.addChildElement("Password");
            password.addTextNode(instanceInfo.getPassword());
            SOAPElement userNameSpace = login.addChildElement("userNameSpace");
            userNameSpace.addTextNode(instanceInfo.getUserNameSpace());

            soapBody.addNamespaceDeclaration("ns0" , namespace);

        }catch(Exception e){
            logger.error("Error generating login request body: ", e);
        }
        return soapBody;
    }
}
