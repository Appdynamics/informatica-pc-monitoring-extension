/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.response;

import com.appdynamics.monitors.informatica.dto.DIServerInfo;
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
public class AllDIServerResponse {

    private static final Logger logger = LoggerFactory.getLogger(AllDIServerResponse.class);

    private SOAPMessage soapResponse;

    public AllDIServerResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    public List<DIServerInfo> getServerInfo(){

        List<DIServerInfo> diServerInfoList = new ArrayList<>();
        try {
            SOAPBody body = soapResponse.getSOAPBody();
            NodeList list = body.getElementsByTagName("DIServerInfo");

            for (int i = 0; i < list.getLength(); i++) {
                NodeList innerList = list.item(i).getChildNodes();

                for (int j = 0; j < innerList.getLength(); j++) {
                    if(innerList.item(j).getNodeName().equals("Name")){
                        DIServerInfo serverInfo = new DIServerInfo();
                        serverInfo.setServiceName(innerList.item(j).getTextContent());
                        diServerInfoList.add(serverInfo);
                    }
                }
            }
        }catch(SOAPException e){
            logger.error("SOAPException retrieving DIServerInfo response: " + e.getMessage());
        }
        return diServerInfoList;
    }
}
