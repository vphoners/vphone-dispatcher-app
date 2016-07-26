package io.vphone.vphonedispatcher;

import java.io.Serializable;

/**
 * Created by mohsen on 7/25/16.
 */
public class Message implements Serializable{

    private String from;
    private String timestamp;
    private String body;
    private String device;

    public Message(String from, String timestamp, String body, String device) {
        this.from = from;
        this.timestamp = timestamp;
        this.body = body;
        this.device = device;
    }

    public String getFrom() {
        return from;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getBody() {
        return body;
    }

    public String getDevice() {
        return device;
    }
}