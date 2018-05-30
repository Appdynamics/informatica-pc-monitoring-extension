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
import com.appdynamics.monitors.informatica.IPMonitorTask;
import com.appdynamics.monitors.informatica.Instance;
import com.appdynamics.monitors.informatica.dto.DIServerInfo;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import com.appdynamics.monitors.informatica.response.AllDIServerResponse;
import com.appdynamics.monitors.informatica.response.PingDIServerResponse;
import com.appdynamics.monitors.informatica.saop.SOAPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    public DIServerTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriterHelper, String metricPrefix, Phaser phaser) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriterHelper;
        this.metricPrefix = metricPrefix;
        this.phaser = phaser;
        phaser.register();
    }

    /**
     * Collects all DI servers, ping their status individually and then invoked allFolderRequest
     */
    public void run() {
        try {
            SOAPMessage soapResponse = SOAPClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.ALLDISERVERS.name(), instance, IPMonitorTask.sessionID, null, null);

            /*String mssg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "   <soapenv:Body>" +
                    "      <soapenv:Fault>" +
                    "         <faultcode>Server</faultcode>" +
                    "         <faultstring>Session ID is not valid.</faultstring>" +
                    "         <detail>" +
                    "            <ns1:WSHFaultDetails xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                    "               <ErrorCode>WSH_95064</ErrorCode>" +
                    "               <ExtendedDetails/>" +
                    "            </ns1:WSHFaultDetails>" +
                    "         </detail>" +
                    "      </soapenv:Fault>" +
                    "   </soapenv:Body>" +
                    "</soapenv:Envelope>";
            InputStream is = new ByteArrayInputStream(mssg.getBytes());
            SOAPMessage responseStr = MessageFactory.newInstance().createMessage(null, is);*/

            AllDIServerResponse allDIServerResponse = new AllDIServerResponse(soapResponse);

            //Having retrieved allDIServers, ping each server one by one with max 2 attempts
            List<DIServerInfo> serverInfoList = allDIServerResponse.getServerInfo();
            for(DIServerInfo serverInfo : serverInfoList){
                serverInfo.setDomainName(instance.getDomainName());
                String serverMetricPrefix = metricPrefix + "|" + serverInfo.getDomainName() + "|" + serverInfo.getServiceName() + "|";

                logger.debug("Creating pingDIServer request");
                soapResponse = SOAPClient.callSoapWebService(instance.getHost() + "DataIntegration", RequestTypeEnum.PINGDISERVER.name(), instance, null, null, serverInfo.getServiceName());

                PingDIServerResponse DIServerResponse = new PingDIServerResponse(soapResponse);

                serverInfo.setStatus(DIServerResponse.getStatus());
                metrics.add(new Metric("DIServerStatus", Integer.toString(serverInfo.getStatus().ordinal()), serverMetricPrefix));
            }

            // Task to get all folders information
            FoldersTask foldersTask = new FoldersTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, serverInfoList);
            contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", foldersTask);
            logger.debug("Registering MetricCollectorTask phaser for " + instance.getDisplayName());

            if (metrics != null && metrics.size() > 0) {
                metricWriterHelper.transformAndPrintMetrics(metrics);
            }
            phaser.arriveAndAwaitAdvance();
        }catch(Exception e){
            logger.error("DIServer flow error: ", e);
        }finally {
            logger.debug("DIServer Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }

}
