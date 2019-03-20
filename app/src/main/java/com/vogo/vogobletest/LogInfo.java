package com.vogo.vogobletest;

import java.util.Date;

public class LogInfo {
    String message;
    Date timestamp;

    public LogInfo(String message,Date timestamp){
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
