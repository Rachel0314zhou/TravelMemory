package com.example.travelmemory.model;

public class Expense {
    private int id;
    private int tripId;
    private String category;
    private double amount;
    private String note;
    private String date;

    public Expense() {}

    public Expense(int id, int tripId, String category, double amount, String note, String date) {
        this.id = id;
        this.tripId = tripId;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
