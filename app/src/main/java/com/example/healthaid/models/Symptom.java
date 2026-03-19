package com.example.healthaid.models;

public class Symptom {
    private final String name;
    private final String subtitle;
    private final int    iconResId;

    public Symptom(String name, String subtitle, int iconResId) {
        this.name     = name;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
    }

    public String getName()     { return name; }
    public String getSubtitle() { return subtitle; }
    public int getIconResId()   { return iconResId; }
}