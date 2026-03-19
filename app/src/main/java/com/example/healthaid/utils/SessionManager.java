package com.example.healthaid.utils;

import com.example.healthaid.models.CaregiverLink;

/**
 * Singleton that tracks whether the current user is viewing their own data
 * or monitoring a patient as a caregiver.
 */
public class SessionManager {

    private static SessionManager instance;

    // The logged-in user's own UID — never changes
    private String ownUserId;
    private String ownName;

    // The patient being monitored (null when viewing own account)
    private String  patientUserId;
    private String  patientName;
    private String  permissionLevel;   // "view_only" or "editor"
    private String  caregiverLinkId;   // for writing back to Firestore

    private boolean caregiverModeActive = false;

    private SessionManager() {}

    public static SessionManager get() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    /** Call this once after login with the logged-in user's own details. */
    public void init(String ownUserId, String ownName) {
        this.ownUserId  = ownUserId;
        this.ownName    = ownName;
        // Reset caregiver state on every new login
        caregiverModeActive = false;
        patientUserId       = null;
        patientName         = null;
        permissionLevel     = null;
        caregiverLinkId     = null;
    }

    /** Switch into caregiver mode to monitor a patient. */
    public void enterCaregiverMode(String patientUserId, String patientName,
                                   String permissionLevel, String caregiverLinkId) {
        this.patientUserId       = patientUserId;
        this.patientName         = patientName;
        this.permissionLevel     = permissionLevel;
        this.caregiverLinkId     = caregiverLinkId;
        this.caregiverModeActive = true;
    }

    /** Switch back to own account view. */
    public void exitCaregiverMode() {
        caregiverModeActive = false;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** The UID whose Firestore data should be loaded — patient or self. */
    public String getActiveUserId() {
        return caregiverModeActive ? patientUserId : ownUserId;
    }

    public String getOwnUserId()       { return ownUserId; }
    public String getOwnName()         { return ownName; }
    public String getPatientName()     { return patientName; }
    public String getPatientUserId()   { return patientUserId; }
    public String getPermissionLevel() { return permissionLevel; }
    public String getCaregiverLinkId() { return caregiverLinkId; }
    public boolean isCaregiverMode()   { return caregiverModeActive; }

    /** True when viewing own account OR when caregiver has editor permission. */
    public boolean canEdit() {
        if (!caregiverModeActive) return true;
        return CaregiverLink.PERMISSION_EDITOR.equals(permissionLevel);
    }

    /** Reset everything on logout. */
    public static void reset() { instance = null; }
}