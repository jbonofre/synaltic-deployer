package com.synaltic.deployer.api;

public class Feature {

    private String name;
    private String version;
    private String state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Name: " + name + ",Version: " + version + ", State: " + state;
    }

}
