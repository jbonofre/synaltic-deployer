package com.synaltic.deployer.rest;

public class ConfigRequest {

    private String pid;
    private String jmxUrl;
    private String karafName;
    private String user;
    private String password;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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
