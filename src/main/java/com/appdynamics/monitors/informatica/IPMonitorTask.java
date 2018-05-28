/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.monitors.informatica.enums.RequestTypeEnum;
import com.appdynamics.monitors.informatica.response.LoginResponse;
import com.appdynamics.monitors.informatica.saop.SOAPClient;
import com.appdynamics.monitors.informatica.tasks.DIServerTask;
import com.appdynamics.monitors.informatica.tasks.FoldersTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.concurrent.Phaser;

/**
 * @author Akshay Srivastava
 */
public class IPMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = LoggerFactory.getLogger(IPMonitorTask.class);

    private MonitorContextConfiguration configuration;

    private Instance instance;

    private MetricWriteHelper metricWriterHelper;

    private String metricPrefix;

    private String displayName;

    private SOAPClient soapClient;

    private static String sessionID;

    public IPMonitorTask(TasksExecutionServiceProvider serviceProvider, MonitorContextConfiguration configuration, Instance instance) {
        this.configuration = configuration;
        this.instance = instance;
        this.metricPrefix = configuration.getMetricPrefix() + "|" + instance.getDisplayName();
        this.metricWriterHelper = serviceProvider.getMetricWriteHelper();
        this.displayName = instance.getDisplayName();
        this.soapClient = new SOAPClient();
    }

    public void run(){
        try {
            BigDecimal loginStatus = BigDecimal.ZERO;
            SOAPMessage response = soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.LOGIN.name(), instance, null, null, null, null);

            /*String mssg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "   <soapenv:Header>" +
                    "      <ns1:Context xmlns:ns1=\"http://www.informatica.com/wsh\">" +
                    "         <SessionId>cd032b685b50477e16321983a5a</SessionId>" +
                    "      </ns1:Context>" +
                    "   </soapenv:Header>" +
                    "   <soapenv:Body>" +
                    "      <ns1:LoginReturn xmlns:ns1=\"http://www.informatica.com/wsh\">cd032b685b50477e16321983a5a</ns1:LoginReturn>" +
                    "   </soapenv:Body>" +
                    "</soapenv:Envelope>";
            InputStream is = new ByteArrayInputStream(mssg.getBytes());
            SOAPMessage responseStr = MessageFactory.newInstance().createMessage(null, is);
            SOAPBody respBody = responseStr.getSOAPBody();*/

            LoginResponse loginResponse = new LoginResponse(response);
            sessionID = loginResponse.getSessionId();
            if(StringUtils.hasText(sessionID)){
                loginStatus = BigDecimal.ONE;
            }
            metricWriterHelper.printMetric(metricPrefix + "Availability", loginStatus, "AVG.AVG.IND");

            Phaser phaser = new Phaser();

            DIServerTask DIServerTask = new DIServerTask(configuration, instance, metricWriterHelper, metricPrefix, phaser, soapClient, sessionID);
            configuration.getContext().getExecutorService().execute("MetricCollectorTask", DIServerTask);
            logger.debug("Registering MetricCollectorTask phaser for " + displayName);

            //Wait for all tasks to finish
            phaser.arriveAndAwaitAdvance();
            logger.info("Completed the Informatica Power Center Monitoring task");

        }catch(Exception e) {
            logger.error("Unexpected error while running the Informatica PowerCenter Monitor", e);
        }
    }

    public void onTaskComplete() {
        logger.info("All tasks for instance {} finished");
        if(sessionID != null){
            soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.LOGOUT.name(), instance, sessionID, null, null, null);
        }
    }

}
