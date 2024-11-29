package com.example.messagingstompwebsocket.controller;

import java.time.LocalDateTime;

import com.example.messagingstompwebsocket.common.ItemProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public class ConsoleItem {

    private long id;

    @JsonProperty(required = true)
    @Size(min = 2, max=40, message = "Session id length min = 2, max = 40")
    //TODO: @Pattern -> no whitespace allowed
    private String sessionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty(required = true)
    @Size(min = 5, max=20, message = "Item type length min = 5, max = 20")
    private String type;

    @JsonProperty(required = true)
    @Size(min = 5, max=20, message = "Item name length min = 5, max = 20")
    private String name;

    private ItemProperty[] itemProperties;

    @Size(max=4000, message = "Raw text max = 4000")
    private String raw;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemProperty[] getItemProperties() {
        return itemProperties;
    }

    public void setItemProperties(ItemProperty[] itemProperties) {
        this.itemProperties = itemProperties;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }



}
