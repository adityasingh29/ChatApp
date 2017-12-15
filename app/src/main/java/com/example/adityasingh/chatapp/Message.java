package com.example.adityasingh.chatapp;

import java.util.Date;

/**
 * Created by adityasingh on 10/12/17.
 */

public class Message {
    private String text;
    private String name;
    private String photoUrl;
    private long messageTime;

    public Message() {
    }

    public Message(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        messageTime= new Date().getTime();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getTime(){return messageTime;}

    public void setMessageTime(long messageTime){this.messageTime=messageTime;}
}
