/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.tasks;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.dto.DIServerInfo;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import com.appdynamics.monitors.informatica.response.AllDIServerResponse;
import com.appdynamics.monitors.informatica.response.PingDIServerResponse;
import com.appdynamics.monitors.informatica.saop.SOAPClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * @author Akshay Srivastava
 */
public class DIServerTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DIServerTask.class);

    private MonitorContextConfiguration contextConfiguration;

    private MetricWriteHelper metricWriterHelper;

    private Instance instance;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String metricPrefix;

    private Phaser phaser;

    private SOAPClient soapClient;

    private String sessionID;

    private ObjectMapper objectMapper = new ObjectMapper();

    private int numOfAttempts =1;

    public List<Metric> getMetrics() {
        return metrics;
    }

    public DIServerTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriterHelper, String metricPrefix, Phaser phaser, SOAPClient soapClient, String sessionID) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriterHelper;
        this.metricPrefix = metricPrefix;
        this.phaser = phaser;
        this.soapClient = soapClient;
        this.sessionID = sessionID;
        phaser.register();
    }

    public void run() {
        try {
            SOAPMessage soapResponse = soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.ALLDISERVERS.name(), instance, sessionID, null, null, null);

            String mssg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "   <soapenv:Header>" +
                    "      <ns1:Context xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                    "         <SessionId>cd032b685b50477e16321983a5a</SessionId>" +
                    "      </ns1:Context>" +
                    "   </soapenv:Header>" +
                    "   <soapenv:Body>" +
                    "      <ns1:GetAllDIServersReturn xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                    "         <DIServerInfo>" +
                    "            <Name>NAGP_IS_ASCII</Name>" +
                    "         </DIServerInfo>" +
                    "         <DIServerInfo>" +
                    "            <Name>NAGP_IS_UNICODE</Name>" +
                    "         </DIServerInfo>" +
                    "      </ns1:GetAllDIServersReturn>" +
                    "   </soapenv:Body>" +
                    "</soapenv:Envelope>";
            InputStream is = new ByteArrayInputStream(mssg.getBytes());
            SOAPMessage responseStr = MessageFactory.newInstance().createMessage(null, is);
            SOAPBody respBody = responseStr.getSOAPBody();

            AllDIServerResponse allDIServerResponse = new AllDIServerResponse(responseStr);

            //Having retrieved allDIServers, ping each server one by one with max 2 attempts

            List<DIServerInfo> serverInfoList = allDIServerResponse.getServerInfo();
            for(DIServerInfo serverInfo : serverInfoList){
                serverInfo.setDomainName(instance.getDomainName());
                String serverMetricPrefix = metricPrefix + "|" + serverInfo.getDomainName() + "|" + serverInfo.getServiceName() + "|";

                logger.debug("Creating pingDIServer request");
                  soapResponse = soapClient.callSoapWebService(instance.getHost() + "DataIntegration", RequestTypeEnum.PINGDISERVER.name(), instance, sessionID, null, null, serverInfo.getServiceName());

                /*mssg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        "   <soapenv:Header>" +
                        "      <ns1:Context xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                        "         <SessionId>cd032b685b50477e16321983a5a</SessionId>" +
                        "      </ns1:Context>" +
                        "   </soapenv:Header>" +
                        "   <soapenv:Body>" +
                        "      <ns1:PingDIServerReturn xmlns:ns1=\"http://www.informatica.com/wsh\">ALIVE</ns1:PingDIServerReturn>" +
                        "   </soapenv:Body>" +
                        "</soapenv:Envelope>";
                is = new ByteArrayInputStream(mssg.getBytes());
                responseStr = MessageFactory.newInstance().createMessage(null, is);*/

                PingDIServerResponse DIServerResponse = new PingDIServerResponse(soapResponse);
                serverInfo.setStatus(DIServerResponse.getStatus());
                metrics.add(new Metric("DIServerStatus", Integer.toString(serverInfo.getStatus().ordinal()), serverMetricPrefix));
            }

            FoldersTask foldersTask = new FoldersTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, soapClient, sessionID, serverInfoList);
            contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", foldersTask);
            logger.debug("Registering MetricCollectorTask phaser for " + instance.getDisplayName());

            if (metrics != null && metrics.size() > 0) {
                metricWriterHelper.transformAndPrintMetrics(metrics);
            }
        }catch(Exception e){
            logger.error("DIServer flow error: " + e.getMessage());
        }finally {
            logger.debug("DIServer Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }

}
