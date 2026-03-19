package com.example.healthaid.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class DoseLog {

    public static final String ACTION_TAKEN   = "taken";
    public static final String ACTION_MISSED  = "missed";
    public static final String ACTION_SNOOZED = "snoozed";

    @DocumentId
    private String id;

    private String    medicationId;   // Firestore doc ID of the parent medication
    private String    pillName;
    private String    action;         // "taken" | "missed" | "snoozed"
    private String    scheduledTime;  // e.g. "08:00 AM"

    @ServerTimestamp
    private Timestamp actionAt;

    public DoseLog() {}

    public DoseLog(String medicationId, String pillName,
                   String action, String scheduledTime) {
        this.medicationId  = medicationId;
        this.pillName      = pillName;
        this.action        = action;
        this.scheduledTime = scheduledTime;
    }

    public String    getId()            { return id; }
    public String    getMedicationId()  { return medicationId; }
    public String    getPillName()      { return pillName; }
    public String    getAction()        { return action; }
    public String    getScheduledTime() { return scheduledTime; }
    public Timestamp getActionAt()      { return actionAt; }

    public void setId(String id)                   { this.id = id; }
    public void setMedicationId(String m)          { this.medicationId = m; }
    public void setPillName(String n)              { this.pillName = n; }
    public void setAction(String a)                { this.action = a; }
    public void setScheduledTime(String t)         { this.scheduledTime = t; }
    public void setActionAt(Timestamp ts)          { this.actionAt = ts; }
}