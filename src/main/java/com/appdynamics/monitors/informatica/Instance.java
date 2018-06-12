/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.informatica;

/**
 * @author Akshay Srivastava
 */
public class Instance {

    private String host;
    private Boolean useSSL;
    private String username;
    private String password;
    private String displayName;
    private String domainName;
    private String repositoryName;
    private String userNameSpace;
    private String encryptedPassword;
    private String encryptionKey;
    private String timeout;
    private int numOfAttempts;
    private String metadataURL;
    private String dataIntegrationURL;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(Boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getUserNameSpace() {
        return userNameSpace;
    }

    public void setUserNameSpace(String userNameSpace) {
        this.userNameSpace = userNameSpace;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getNumOfAttempts() {
        return numOfAttempts;
    }

    public void setNumOfAttempts(int numOfAttempts) {
        this.numOfAttempts = numOfAttempts;
    }

    public String getMetadataURL() {
        return metadataURL;
    }

    public void setMetadataURL(String metadataURL) {
        this.metadataURL = metadataURL;
    }

    public String getDataIntegrationURL() {
        return dataIntegrationURL;
    }

    public void setDataIntegrationURL(String dataIntegrationURL) {
        this.dataIntegrationURL = dataIntegrationURL;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("host : " + host);
        builder.append("|");
        builder.append(" useSSL : " + useSSL.toString());
        builder.append("|");
        builder.append(" username : " + username);
        builder.append("|");
        builder.append(" domainName : " + domainName);
        builder.append("|");
        builder.append(" repositoryName : " + repositoryName);
        builder.append("|");
        return builder.toString();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
