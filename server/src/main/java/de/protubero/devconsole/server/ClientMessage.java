package de.protubero.devconsole.server;

public class ClientMessage {

    private String command;
    private Object payload;

    public ClientMessage(String command, Object payload) {
        this.command = command;
        this.payload = payload;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
