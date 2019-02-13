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
import com.appdynamics.monitors.informatica.dataIntegration.DIServiceInfo;
import com.appdynamics.monitors.informatica.dataIntegration.DataIntegrationInterface;
import com.appdynamics.monitors.informatica.dataIntegration.DataIntegrationService;
import com.appdynamics.monitors.informatica.dataIntegration.EPingState;
import com.appdynamics.monitors.informatica.dataIntegration.PingDIServerRequest;
import com.appdynamics.monitors.informatica.metadata.DIServerInfo;
import com.appdynamics.monitors.informatica.metadata.DIServerInfoArray;
import com.appdynamics.monitors.informatica.metadata.MetadataInterface;
import com.appdynamics.monitors.informatica.metadata.MetadataService;
import com.appdynamics.monitors.informatica.metadata.VoidRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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

    private URL metadataURL;

    private URL dataIntegrationURL;

    private Phaser phaser;

    public DIServerTask(MonitorContextConfiguration contextConfiguration, Instance instance, MetricWriteHelper metricWriterHelper, String metricPrefix, Phaser phaser, URL metadataURL, URL dataIntegrationURL) {
        this.contextConfiguration = contextConfiguration;
        this.instance = instance;
        this.metricWriterHelper = metricWriterHelper;
        this.metricPrefix = metricPrefix;
        phaser.register();
        this.phaser = phaser;
        this.metadataURL = metadataURL;
        this.dataIntegrationURL = dataIntegrationURL;
    }

    /**
     * Collects all DI servers, ping their status individually and then invoked allFolderRequest
     */
    public void run() {
        try {

            MetadataService service = new MetadataService(metadataURL);

            MetadataInterface server = service.getMetadata();

            VoidRequest allDIServerReq = new VoidRequest();


            //Retrieve allDIServers, ping each server one by one with max 2 attempts
            DIServerInfoArray serverInfoList = server.getAllDIServers(allDIServerReq);

            for (DIServerInfo serverInfo : serverInfoList.getDIServerInfo()) {

                String serverMetricPrefix = metricPrefix + "|" + instance.getDomainName() + "|" + serverInfo.getName() + "|";

                logger.debug("Creating pingDIServer request");

                DataIntegrationService DIservice = new DataIntegrationService(dataIntegrationURL);

                DataIntegrationInterface DIServer = DIservice.getDataIntegration();

                PingDIServerRequest pingRequest = new PingDIServerRequest();

                DIServiceInfo diServiceInfo = new DIServiceInfo();
                diServiceInfo.setDomainName(instance.getDomainName());
                diServiceInfo.setServiceName(serverInfo.getName());

                pingRequest.setDIServiceInfo(diServiceInfo);
                pingRequest.setTimeOut(Integer.parseInt(instance.getTimeout()));

                EPingState pingState = DIServer.pingDIServer(pingRequest);
                logger.debug("Ping state retrieved for " + serverInfo.getName() + ": " + pingState.value());

                metrics.add(new Metric("DIServerStatus", Integer.toString(pingState.ordinal()), serverMetricPrefix));
            }

            // Task to get all folders information
            FoldersTask foldersTask = new FoldersTask(contextConfiguration, instance, metricWriterHelper, metricPrefix, phaser, serverInfoList, metadataURL, dataIntegrationURL);
            contextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", foldersTask);
            logger.debug("Registering MetricCollectorTask phaser for " + instance.getDisplayName());

            if (metrics != null && metrics.size() > 0) {
                metricWriterHelper.transformAndPrintMetrics(metrics);
            }
            phaser.arriveAndAwaitAdvance();
        } catch (Exception e) {
            logger.error("DIServer flow error: ", e);
        } finally {
            logger.debug("DIServer Phaser arrived for {}", instance.getDisplayName());
            phaser.arriveAndDeregister();
        }
    }

}
