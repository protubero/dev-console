package com.example.messagingstompwebsocket.controller;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionInfo {


    @JsonProperty(required = true)
    private String sessionId;

    private String name;

    private Map<String, String> properties;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
