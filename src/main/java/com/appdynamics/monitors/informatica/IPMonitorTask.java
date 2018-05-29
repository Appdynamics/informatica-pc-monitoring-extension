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

    /**
     * Attempts Login, on success starts DIServer thread, on failure, reports Availability metric.
     */
    public void run(){

        Phaser phaser = new Phaser();
        try {
            BigDecimal loginStatus = BigDecimal.ZERO;
            SOAPMessage response = soapClient.callSoapWebService(instance.getHost() + "Metadata", RequestTypeEnum.LOGIN.name(), instance, null, null, null, null);

            LoginResponse loginResponse = new LoginResponse(response);
            sessionID = loginResponse.getSessionId();

            if(StringUtils.hasText(sessionID)){
                loginStatus = BigDecimal.ONE;

                DIServerTask DIServerTask = new DIServerTask(configuration, instance, metricWriterHelper, metricPrefix, phaser, soapClient, sessionID);
                configuration.getContext().getExecutorService().execute("MetricCollectorTask", DIServerTask);
                logger.debug("Registering MetricCollectorTask phaser for " + displayName);
            }
            //Wait for all tasks to finish
            int phase = phaser.arriveAndAwaitAdvance();
            System.out.println(phase);
            metricWriterHelper.printMetric(metricPrefix + "|Availability", loginStatus, "AVG.AVG.IND");
            logger.info("Completed the Informatica Power Center Monitoring task");

        }catch(Exception e) {
            logger.error("Unexpected error while running the Informatica PowerCenter Monitor", e);
        }
    }

    public void onTaskComplete() {
        logger.info("All tasks for instance {} finished");
    }

}
