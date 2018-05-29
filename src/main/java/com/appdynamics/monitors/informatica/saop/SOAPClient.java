/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.saop;

import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.request.BaseRequest;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;

/**
 * @author Akshay Srivastava
 */
public class SOAPClient {

    private static String namespaceXSI = "xsi";
    private static String namespaceXSIURI = "http://www.w3.org/2001/XMLSchema-instance";
    private static String namespaceXSD = "xsd";
    private static String namespaceXSDURI = "http://www.w3.org/2001/XMLSchema";
    private static String contextNamespace = "http://www.informatica.com/wsh";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SOAPClient.class);

    /**
     * Creates SOAP request based on requestType
     * @param requestType
     * @param instanceInfo
     * @param sessionID
     * @return
     * @throws Exception
     */
    public static SOAPMessage createSOAPRequest(String requestType, Instance instanceInfo, String sessionID, String folderName, String workflowName, String serverName) throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        createSoapEnvelope(soapMessage, requestType, instanceInfo, sessionID, folderName, workflowName, serverName);
        soapMessage.saveChanges();

        if(logger.isDebugEnabled()){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            logger.debug( requestType + " SOAP Request being sent: " + out.toString());
        }

        //soapMessage.writeTo(System.out);
        return soapMessage;
    }

    /**
     * Invokes the endpoint with the soap request
     * @param soapEndpointUrl
     * @param soapAction
     * @param instanceInfo
     * @param sessionID
     * @return SOAPMessage
     */
    public static SOAPMessage callSoapWebService(String soapEndpointUrl, String soapAction, Instance instanceInfo, String sessionID, String folderName, String workflowName, String serverName) {

        SOAPMessage soapResponse = null;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            soapResponse = soapConnection.call(createSOAPRequest(soapAction, instanceInfo, sessionID, folderName, workflowName, serverName), soapEndpointUrl);

            if(logger.isDebugEnabled()){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                soapResponse.writeTo(System.out);
                logger.debug( " SOAP Response received: " + out.toString());
            }
            soapConnection.close();

        } catch (Exception e) {
            logger.error("Error occurred invoking SOAP Request: ", e);
        }
        return soapResponse;
    }

    /**
     * Creates SOAP envelope for given SOAP message and request type
     * @param soapMessage
     * @param requestType
     * @param instanceInfo
     * @param sessionID
     * @throws SOAPException
     */
    private static void createSoapEnvelope(SOAPMessage soapMessage, String requestType, Instance instanceInfo, String sessionID, String folderName, String workflowName, String serverName) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        //envelope.addNamespaceDeclaration(namespaceSoap, namespaceSoapURI);
        envelope.addNamespaceDeclaration(namespaceXSD, namespaceXSDURI);
        envelope.addNamespaceDeclaration(namespaceXSI, namespaceXSIURI);

        if(sessionID!=null) {
            SOAPHeader header = envelope.getHeader();
            //header.addNamespaceDeclaration("ns0", contextNamespace);
            SOAPHeaderElement context = header.addHeaderElement(new QName(contextNamespace, "Context", "ns0"));
            //context.addNamespaceDeclaration("ns0", contextNamespace);
            SOAPElement sessionIDElement = context.addChildElement("SessionId");
            sessionIDElement.addTextNode(sessionID);
        }
            /*
            Constructed SOAP Request Message:
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:myNamespace="http://www.webserviceX.NET">
                <SOAP-ENV:Header/>
                <SOAP-ENV:Body>
                    <myNamespace:GetInfoByCity>
                        <myNamespace:USCity>New York</myNamespace:USCity>
                    </myNamespace:GetInfoByCity>
                </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            */

        RequestTypeEnum requestTypeEnum = RequestTypeEnum.valueOf(requestType);
        BaseRequest baseRequest = new BaseRequest(envelope);

        SOAPBody soapBody = baseRequest.getRequestBody(requestTypeEnum, instanceInfo, folderName, workflowName, serverName);
        //envelope.addBody(soapBody);

        // SOAP Body
        /*SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("GetInfoByCity", namespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("USCity", namespace);
        soapBodyElem1.addTextNode("New York");*/
    }
/*
    public static void main(String[] args){
        try {
            //Create a SOAPConnection
           *//* SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();

            SOAPConnection connection = factory.createConnection();

            SOAPClient client  = new SOAPClient();
            client.createSOAPRequest(RequestTypeEnum.LOGIN.name());*//*
*//*          //Create a SOAPMessage
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage message = messageFactory.createMessage();
            SOAPPart  soapPart = message.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPHeader header = envelope.getHeader();
            SOAPBody body = envelope.getBody();
            header.detachNode();

            //Create a SOAPBodyElement
            Name bodyName = envelope.createName("GetLastTradePrice","m", "http://wombat.ztrade.com");
            SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
            //Insert Content
            Name name = envelope.createName("symbol");
            SOAPElement symbol = bodyElement.addChildElement(name);
            symbol.addTextNode("SUNW");

             Create an endpint point which is either URL or String type
            URL endpoint = new URL("http://wombat.ztrade.com/quotes");

            //Send a SOAPMessage (request) and then wait for SOAPMessage (response)
            SOAMessage response= connection.call(message, endpoint);*//**//*

            // Close the SOAPConnection
            connection.close();*//*

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }*/

}



