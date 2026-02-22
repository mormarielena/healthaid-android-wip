package com.example.healthaid;

public class PillReminder {
    private String pillName;
    private String time;

    public PillReminder(String pillName, String time) {
        this.pillName = pillName;
        this.time = time;
    }

    public String getPillName() {
        return pillName;
    }

    public String getTime() {
        return time;
    }
}