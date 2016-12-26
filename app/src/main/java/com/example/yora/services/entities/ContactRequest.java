package com.example.yora.services.entities;

import java.util.Calendar;

public class ContactRequest {
    private int _id;
    private boolean _isFromUs;
    private UserDetails _user;
    private Calendar _createdAt;

    public ContactRequest(int id, boolean isFromUs, UserDetails user, Calendar createdAt) {
        _id = id;
        _isFromUs = isFromUs;
        _user = user;
        _createdAt = createdAt;
    }

    public int getId() {
        return _id;
    }

    public boolean isFromUs() {
        return _isFromUs;
    }

    public UserDetails getUser() {
        return _user;
    }

    public Calendar getCreatedAt() {
        return _createdAt;
    }
}
