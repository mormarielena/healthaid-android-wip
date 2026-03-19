package com.example.healthaid.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PillReminder {

    @DocumentId
    private String id;

    private String  pillName;
    private String  dosage;
    private String  unit;
    private String  time;
    private boolean taken;
    private String  takenDate;       // "yyyy-MM-dd" — compared to today to auto-reset
    private boolean isActive;

    // Refill tracking
    private int pillsRemaining;
    private int lowStockThreshold;

    @ServerTimestamp
    private Timestamp createdAt;

    public PillReminder() {}

    public PillReminder(String pillName, String dosage, String unit, String time) {
        this.pillName          = pillName;
        this.dosage            = dosage;
        this.unit              = unit;
        this.time              = time;
        this.taken             = false;
        this.takenDate         = "";
        this.isActive          = true;
        this.pillsRemaining    = 0;
        this.lowStockThreshold = 7;
    }

    // ─── Today's date string ─────────────────────────────────────────────────

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    // ─── Has this pill been taken TODAY? ─────────────────────────────────────

    public boolean isTakenToday() {
        return taken && today().equals(takenDate);
    }

    // Getters
    public String    getId()                { return id; }
    public String    getPillName()          { return pillName; }
    public String    getDosage()            { return dosage; }
    public String    getUnit()              { return unit; }
    public String    getTime()              { return time; }
    public boolean   isTaken()             { return taken; }
    public String    getTakenDate()        { return takenDate; }
    public int       getPillsRemaining()    { return pillsRemaining; }
    public int       getLowStockThreshold() { return lowStockThreshold; }
    public Timestamp getCreatedAt()         { return createdAt; }

    @PropertyName("isActive")
    public boolean getIsActive()            { return isActive; }

    // Setters
    public void setId(String id)                  { this.id = id; }
    public void setPillName(String n)             { this.pillName = n; }
    public void setDosage(String d)               { this.dosage = d; }
    public void setUnit(String u)                 { this.unit = u; }
    public void setTime(String t)                 { this.time = t; }
    public void setTaken(boolean taken)           { this.taken = taken; }
    public void setTakenDate(String d)            { this.takenDate = d; }
    public void setPillsRemaining(int p)          { this.pillsRemaining = p; }
    public void setLowStockThreshold(int t)       { this.lowStockThreshold = t; }
    public void setCreatedAt(Timestamp ts)        { this.createdAt = ts; }

    @PropertyName("isActive")
    public void setActive(boolean active)         { this.isActive = active; }

    public boolean isLowStock() {
        return pillsRemaining > 0 && pillsRemaining <= lowStockThreshold;
    }
}