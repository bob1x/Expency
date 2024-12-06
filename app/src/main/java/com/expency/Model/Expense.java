package com.expency.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Expense implements Parcelable {
    private String id;
    private double amount;
    private String description;
    private String date;
    private String category;
    public Expense(String id, double amount, String description, String date, String category) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
    }

    public Expense() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return category +" - "+ description + " - " + amount + "TND" + " - " + date ;  // Display description and amount only
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    protected Expense(Parcel in) {
        id = in.readString();
        amount = in.readDouble();
        description = in.readString();
        date = in.readString();
        category = in.readString();
    }

    public static final Creator<Expense> CREATOR = new Creator<Expense>() {
        @Override
        public Expense createFromParcel(Parcel in) {
            return new Expense(in);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(amount);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(category);
    }
}

