package com.example.kushal.cloudsightrequest;

import java.util.ArrayList;

/**
 * Created by Kushal on 2/7/2016.
 */
public class CloudSightRecognitionResponse {
    private String status;
    private String name;
    private ArrayList<String> flags;
    private String reason;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getFlags() {
        return flags;
    }

    public void setFlags(ArrayList<String> flags) {
        this.flags = flags;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean notCompleted() {
        return status.equalsIgnoreCase("not completed");
    }

    public boolean isTimedOut() {
        return status.equalsIgnoreCase("timeout");
    }
}
