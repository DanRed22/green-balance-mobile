package com.example.greenbalance.model;

public class NotebookItemModel {
    private String id;
    private String title;
    private String description;
    private Float total;

    public NotebookItemModel(String id, String title, String description, Float total) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.total = total;
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

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }
}
