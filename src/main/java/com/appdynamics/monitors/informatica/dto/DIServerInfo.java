/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.dto;

import com.appdynamics.monitors.informatica.enums.DIServerStatusEnum;

/**
 * @author Akshay Srivastava
 */
public class DIServerInfo {

    private String domainName;
    private String serviceName;
    private DIServerStatusEnum status;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public DIServerStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DIServerStatusEnum status) {
        this.status = status;
    }
}
