package com.example.healthaid;

public class Symptom {
    private String name;
    private int iconResId;

    public Symptom(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}