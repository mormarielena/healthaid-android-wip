package com.example.healthaid;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

public class PillReminder {

    @DocumentId
    private String id;

    private String  pillName;
    private String  dosage;
    private String  unit;
    private String  time;
    private boolean taken;
    private boolean isActive;

    // Refill tracking
    private int pillsRemaining;
    private int lowStockThreshold;   // warn when pillsRemaining drops to this

    @ServerTimestamp
    private Timestamp createdAt;

    // Required no-arg constructor for Firestore
    public PillReminder() {}

    public PillReminder(String pillName, String dosage, String unit, String time) {
        this.pillName          = pillName;
        this.dosage            = dosage;
        this.unit              = unit;
        this.time              = time;
        this.taken             = false;
        this.isActive          = true;
        this.pillsRemaining    = 0;
        this.lowStockThreshold = 7;   // default: warn when 7 pills left (1 week)
    }

    // Getters
    public String    getId()                 { return id; }
    public String    getPillName()           { return pillName; }
    public String    getDosage()             { return dosage; }
    public String    getUnit()               { return unit; }
    public String    getTime()               { return time; }
    public boolean   isTaken()               { return taken; }
    @PropertyName("isActive")
    public boolean   getIsActive()            { return isActive; }
    public int       getPillsRemaining()     { return pillsRemaining; }
    public int       getLowStockThreshold()  { return lowStockThreshold; }
    public Timestamp getCreatedAt()          { return createdAt; }

    // Setters
    public void setId(String id)                        { this.id = id; }
    public void setPillName(String n)                   { this.pillName = n; }
    public void setDosage(String d)                     { this.dosage = d; }
    public void setUnit(String u)                       { this.unit = u; }
    public void setTime(String t)                       { this.time = t; }
    public void setTaken(boolean taken)                 { this.taken = taken; }
    @PropertyName("isActive")
    public void setActive(boolean active)               { this.isActive = active; }
    public void setPillsRemaining(int p)                { this.pillsRemaining = p; }
    public void setLowStockThreshold(int t)             { this.lowStockThreshold = t; }
    public void setCreatedAt(Timestamp ts)              { this.createdAt = ts; }

    // Convenience: is stock low?
    public boolean isLowStock() {
        return pillsRemaining > 0 && pillsRemaining <= lowStockThreshold;
    }
}