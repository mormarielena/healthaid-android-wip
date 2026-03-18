package com.example.healthaid;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class PillReminder {

    @DocumentId
    private String id;

    private String pillName;
    private String dosage;
    private String unit;
    private String time;
    private boolean taken;
    private boolean isActive;

    @ServerTimestamp
    private Timestamp createdAt;

    public PillReminder() {}

    public PillReminder(String pillName, String dosage, String unit, String time) {
        this.pillName = pillName;
        this.dosage   = dosage;
        this.unit     = unit;
        this.time     = time;
        this.taken    = false;
        this.isActive = true;
    }

    // Getters
    public String getId()        { return id; }
    public String getPillName()  { return pillName; }
    public String getDosage()    { return dosage; }
    public String getUnit()      { return unit; }
    public String getTime()      { return time; }
    public boolean isTaken()     { return taken; }
    public boolean isActive()    { return isActive; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id)           { this.id = id; }
    public void setPillName(String n)      { this.pillName = n; }
    public void setDosage(String d)        { this.dosage = d; }
    public void setUnit(String u)          { this.unit = u; }
    public void setTime(String t)          { this.time = t; }
    public void setTaken(boolean taken)    { this.taken = taken; }
    public void setActive(boolean active)  { this.isActive = active; }
    public void setCreatedAt(Timestamp ts) { this.createdAt = ts; }
}