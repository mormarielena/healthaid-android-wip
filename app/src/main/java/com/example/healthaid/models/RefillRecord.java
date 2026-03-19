package com.example.healthaid.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class RefillRecord {

    @DocumentId
    private String id;

    private String    medicationId;
    private String    pillName;          // denormalised for display
    private int       pillsAdded;
    private int       pillsTotalAfter;   // running total after this refill
    private String    pharmacyName;
    private String    prescriptionRef;
    private String    notes;

    @ServerTimestamp
    private Timestamp refillDate;

    private Timestamp nextRefillDate;    // optional — set by user

    public RefillRecord() {}

    public RefillRecord(String medicationId, String pillName,
                        int pillsAdded, int pillsTotalAfter,
                        String pharmacyName, String prescriptionRef, String notes) {
        this.medicationId    = medicationId;
        this.pillName        = pillName;
        this.pillsAdded      = pillsAdded;
        this.pillsTotalAfter = pillsTotalAfter;
        this.pharmacyName    = pharmacyName;
        this.prescriptionRef = prescriptionRef;
        this.notes           = notes;
    }

    public String    getId()              { return id; }
    public String    getMedicationId()    { return medicationId; }
    public String    getPillName()        { return pillName; }
    public int       getPillsAdded()      { return pillsAdded; }
    public int       getPillsTotalAfter() { return pillsTotalAfter; }
    public String    getPharmacyName()    { return pharmacyName; }
    public String    getPrescriptionRef() { return prescriptionRef; }
    public String    getNotes()           { return notes; }
    public Timestamp getRefillDate()      { return refillDate; }
    public Timestamp getNextRefillDate()  { return nextRefillDate; }

    public void setId(String id)                     { this.id = id; }
    public void setMedicationId(String m)            { this.medicationId = m; }
    public void setPillName(String n)                { this.pillName = n; }
    public void setPillsAdded(int p)                 { this.pillsAdded = p; }
    public void setPillsTotalAfter(int p)            { this.pillsTotalAfter = p; }
    public void setPharmacyName(String p)            { this.pharmacyName = p; }
    public void setPrescriptionRef(String r)         { this.prescriptionRef = r; }
    public void setNotes(String n)                   { this.notes = n; }
    public void setRefillDate(Timestamp t)           { this.refillDate = t; }
    public void setNextRefillDate(Timestamp t)       { this.nextRefillDate = t; }
}