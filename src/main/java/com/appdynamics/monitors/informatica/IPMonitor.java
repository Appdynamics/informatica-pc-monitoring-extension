/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Akshay Srivastava
 */
public class IPMonitor extends ABaseMonitor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IPMonitor.class);
    private static final String METRIC_PREFIX = "Custom Metrics|InformaticaPowerCenter|";


    @Override
    protected String getDefaultMetricPrefix() {
        return METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return "Informatica PowerCenter Monitor";
    }


    @Override
    protected void doRun(TasksExecutionServiceProvider serviceProvider) {


        List<Instance> instances = initialiseInstances();

        for (Instance instance : instances) {
            logger.info("Monitor started for instance:  " + instance.getDisplayName());
            IPMonitorTask task = new IPMonitorTask(serviceProvider, this.getContextConfiguration(), instance);
            AssertUtils.assertNotNull(instance.getDisplayName(), "The displayName can not be null");
            serviceProvider.submit(instance.getDisplayName(), task);
        }
    }

    private List<Instance> initialiseInstances() {

        List<Instance> instances = new ArrayList<Instance>();

        List<Map> servers = (List<Map>) this.getContextConfiguration().getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");

        if (servers != null && servers.size() > 0) {
            for (Map<String, ?> server : servers) {
                Instance instance = new Instance();
                if (Strings.isNullOrEmpty((String) server.get("displayName"))) {
                    logger.error("Display name not mentioned for server ");
                    throw new RuntimeException("Display name not mentioned for server");
                } else {
                    instance.setDisplayName((String) server.get("displayName"));
                }

                AssertUtils.assertNotNull(server.get("host"), "The 'host name is not initialised");
                instance.setHost((String) server.get("host"));

                AssertUtils.assertNotNull(server.get("username"), "The 'username is not initialised");
                instance.setUsername((String) server.get("username"));


                if (!Strings.isNullOrEmpty((String) server.get("password"))) {
                    instance.setPassword((String) server.get("password"));
                } else if (!Strings.isNullOrEmpty((String) server.get("encryptedPassword"))) {
                    try {
                        Map<String, String> args = Maps.newHashMap();
                        args.put(TaskInputArgs.ENCRYPTED_PASSWORD, (String) server.get("encryptedPassword"));
                        args.put(TaskInputArgs.ENCRYPTION_KEY, (String) this.getContextConfiguration().getConfigYml().get("encryptionKey"));
                        instance.setPassword(CryptoUtil.getPassword(args));

                    } catch (IllegalArgumentException e) {
                        String msg = "Encryption Key not specified. Please set the value in config.yml.";
                        logger.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }

                AssertUtils.assertNotNull(server.get("domainName"), "The domainName is not initialised");
                instance.setDomainName((String) server.get("domainName"));

                AssertUtils.assertNotNull(server.get("repositoryName"), "The repositoryName is not initialised");
                instance.setRepositoryName((String) server.get("repositoryName"));

                AssertUtils.assertNotNull(server.get("userNameSpace"), "The userNameSpace is not initialised");
                instance.setUserNameSpace((String) server.get("userNameSpace"));

                if (server.get("useSSL") != null) {
                    instance.setUseSSL((Boolean) server.get("useSSL"));
                } else {
                    instance.setUseSSL(false);
                }
                instances.add(instance);
            }
        } else {
            logger.error("no instances configured");
        }
        return instances;
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

/*    public static void main(String[] args) throws TaskExecutionException {

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level.DEBUG);

        //logger.getRootLogger().addAppender(ca);

        final IPMonitor monitor = new IPMonitor();

        final Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put("config-file", "/Users/akshay.srivastava/AppDynamics/extensions/informatica-powercenter-monitoring-extension/src/main/resources/conf/config.yml");

        //monitor.execute(taskArgs, null);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    monitor.execute(taskArgs, null);
                } catch (Exception e) {
                    logger.error("Error while running the task", e);
                }
            }
        }, 2, 30, TimeUnit.SECONDS);
    }*/
}
