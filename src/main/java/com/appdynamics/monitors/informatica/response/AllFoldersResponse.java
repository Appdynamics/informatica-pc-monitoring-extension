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
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Akshay Srivastava
 */
public class AllFoldersResponse {

    private static final Logger logger = LoggerFactory.getLogger(AllFoldersResponse.class);

    private SOAPMessage soapResponse;

    public AllFoldersResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    public List<String> getFoldersInfo(){

        List<String> foldersList = new ArrayList<>();
        try {
            SOAPBody body = soapResponse.getSOAPBody();
            NodeList list = body.getElementsByTagName("FolderInfo");

            for (int i = 0; i < list.getLength(); i++) {
                NodeList innerList = list.item(i).getChildNodes();

                for (int j = 0; j < innerList.getLength(); j++) {
                    if(innerList.item(j).getNodeName().equals("Name")){
                        foldersList.add(innerList.item(j).getTextContent());
                    }
                }
            }
        }catch(SOAPException e){
            logger.error("SOAPException retrieving allFolders response: " + e.getMessage());
        }
        return foldersList;
    }
}
