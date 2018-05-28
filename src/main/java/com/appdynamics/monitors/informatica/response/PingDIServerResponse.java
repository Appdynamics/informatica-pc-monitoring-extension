/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.response;

import com.appdynamics.monitors.informatica.enums.DIServerStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Akshay Srivastava
 */
public class PingDIServerResponse {

    private static final Logger logger = LoggerFactory.getLogger(PingDIServerResponse.class);

    private SOAPMessage soapResponse;

    public PingDIServerResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    public DIServerStatusEnum getStatus(){

        DIServerStatusEnum status = DIServerStatusEnum.ALIVE;
        try {
            SOAPBody body = soapResponse.getSOAPBody();
            status = DIServerStatusEnum.valueOf(body.getFirstChild().getNextSibling().getFirstChild().getNodeValue());
        }catch(SOAPException e){
            logger.error("SOAPException retrieving pingDIServer response: ", e);
        }
        return status;
    }
}
