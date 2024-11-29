package de.protubero.devconsole.model;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConsoleItem {

    private long id;

    @NotNull
    @Size(min = 2, max=40, message = "Session id length min = 2, max = 40")
    //TODO: @Pattern -> no whitespace allowed
    private String sessionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull
    @Size(min = 2, max=50, message = "Item type length min = 2, max = 50")
    private String type;

    @NotNull
    @Size(min = 5, max=50, message = "Item name length min = 5, max = 50")
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


    @Override
    public String toString() {
        return "ConsoleItem{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", itemProperties=" + Arrays.toString(itemProperties) +
                ", raw='" + raw + '\'' +
                '}';
    }
}
