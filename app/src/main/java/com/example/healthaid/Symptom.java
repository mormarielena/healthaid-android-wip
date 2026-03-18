package com.example.healthaid;

public class Symptom {
    private String name;
    private String subtitle;
    private int    iconResId;

    public Symptom(String name, String subtitle, int iconResId) {
        this.name     = name;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
    }

    public String getName()     { return name; }
    public String getSubtitle() { return subtitle; }
    public int getIconResId()   { return iconResId; }
}