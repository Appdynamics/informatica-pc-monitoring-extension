/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import java.util.Iterator;

/**
 * @author Akshay Srivastava
 */
public class LoginResponse {

    private static final Logger logger = LoggerFactory.getLogger(LoginResponse.class);

    private SOAPMessage soapResponse;

    public LoginResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    public String getSessionId(){

        String sessionId = "";
        try {
            SOAPBody body = soapResponse.getSOAPBody();
            sessionId = body.getFirstChild().getNextSibling().getFirstChild().getNodeValue();
            logger.debug("Returning session ID: " + sessionId);
        }catch(SOAPException e){
            logger.error("SOAPException retrieving login response: ", e);
        }
        return sessionId;
    }
}
