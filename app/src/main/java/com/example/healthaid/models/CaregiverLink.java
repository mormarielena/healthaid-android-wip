package com.example.healthaid.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class CaregiverLink {

    public static final String PERMISSION_VIEW_ONLY = "view_only";
    public static final String PERMISSION_EDITOR    = "editor";

    @DocumentId
    private String id;

    private String    patientUserId;
    private String    caregiverUserId;
    private String    caregiverEmail;    // denormalised for display
    private String    caregiverName;     // denormalised for display
    private String    patientName;       // denormalised so caregiver sees who they monitor
    private String    permissionLevel;
    private boolean   isActive;
    private Timestamp linkedAt;

    public CaregiverLink() {}

    public CaregiverLink(String patientUserId, String caregiverUserId,
                         String caregiverEmail, String permissionLevel) {
        this.patientUserId   = patientUserId;
        this.caregiverUserId = caregiverUserId;
        this.caregiverEmail  = caregiverEmail;
        this.permissionLevel = permissionLevel;
        this.isActive        = true;
    }

    public String    getId()              { return id; }
    public String    getPatientUserId()   { return patientUserId; }
    public String    getCaregiverUserId() { return caregiverUserId; }
    public String    getCaregiverEmail()  { return caregiverEmail; }
    public String    getCaregiverName()   { return caregiverName; }
    public String    getPatientName()     { return patientName; }
    public String    getPermissionLevel() { return permissionLevel; }
    @PropertyName("isActive")
    public boolean   getIsActive()         { return isActive; }
    public Timestamp getLinkedAt()        { return linkedAt; }

    public void setId(String id)                   { this.id = id; }
    public void setPatientUserId(String p)         { this.patientUserId = p; }
    public void setCaregiverUserId(String c)       { this.caregiverUserId = c; }
    public void setCaregiverEmail(String e)        { this.caregiverEmail = e; }
    public void setCaregiverName(String n)         { this.caregiverName = n; }
    public void setPatientName(String n)           { this.patientName = n; }
    public void setPermissionLevel(String p)       { this.permissionLevel = p; }
    @PropertyName("isActive")
    public void setActive(boolean a)               { this.isActive = a; }
    public void setLinkedAt(Timestamp t)           { this.linkedAt = t; }
}