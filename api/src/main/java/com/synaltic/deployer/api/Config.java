package com.synaltic.deployer.api;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private String pid;
    private Map<String, String> properties;

    public Config() {
        properties = new HashMap<String, String>();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
