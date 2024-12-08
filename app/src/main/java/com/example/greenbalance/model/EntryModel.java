package com.example.greenbalance.model;

public class EntryModel {
    private String id;
    private String title;
    private String description;
    private Float value;
    private String createdAt;

    public EntryModel(String id, String title, String description, Float value, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.value = value;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
