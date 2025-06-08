package com.example.notation;

import java.util.Date;

public class Reminder {

    private String text;
    private Date date;

    public Reminder(String text, Date date) {
        this.text = text;
        this.date = date;
    }

    public Reminder(){
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
