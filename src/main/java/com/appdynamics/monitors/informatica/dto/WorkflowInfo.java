/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica.dto;

import com.appdynamics.monitors.informatica.enums.WorkflowRunStatusEnum;
import com.appdynamics.monitors.informatica.enums.WorkflowRunTypeEnum;

/**
 * @author Akshay Srivastava
 */
public class WorkflowInfo {

    private String name;
    private boolean isValid;
    private String folderName;
    private WorkflowRunStatusEnum workflowRunStatus;
    private WorkflowRunTypeEnum workflowRunType;
    private long workflowRunID;
    private long runErrorCode;

    public WorkflowInfo() {
    }

    public WorkflowInfo(String name, String folderName) {
        this.name = name;
        this.folderName = folderName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public WorkflowRunStatusEnum getWorkflowRunStatus() {
        return workflowRunStatus;
    }

    public void setWorkflowRunStatus(WorkflowRunStatusEnum workflowRunStatus) {
        this.workflowRunStatus = workflowRunStatus;
    }

    public WorkflowRunTypeEnum getWorkflowRunType() {
        return workflowRunType;
    }

    public void setWorkflowRunType(WorkflowRunTypeEnum workflowRunType) {
        this.workflowRunType = workflowRunType;
    }

    public long getWorkflowRunID() {
        return workflowRunID;
    }

    public void setWorkflowRunID(long workflowRunID) {
        this.workflowRunID = workflowRunID;
    }

    public long getRunErrorCode() {
        return runErrorCode;
    }

    public void setRunErrorCode(long runErrorCode) {
        this.runErrorCode = runErrorCode;
    }
}
