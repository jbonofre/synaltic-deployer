package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Config;

public class ConfigUpdateRequest {

    private Config config;
    private String jmxUrl;
    private String karafName;
    private String user;
    private String password;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getJmxUrl() {
        return jmxUrl;
    }

    public void setJmxUrl(String jmxUrl) {
        this.jmxUrl = jmxUrl;
    }

    public String getKarafName() {
        return karafName;
    }

    public void setKarafName(String karafName) {
        this.karafName = karafName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
