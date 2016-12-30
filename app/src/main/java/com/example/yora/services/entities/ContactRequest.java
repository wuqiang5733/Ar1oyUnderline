package com.example.yora.services.entities;

import java.util.Calendar;

public class ContactRequest {
    private boolean _isFromUs;
    private UserDetails _user;
    private Calendar _createdAt;

    public ContactRequest(boolean isFromUs, UserDetails user, Calendar createdAt) {
        _isFromUs = isFromUs;
        _user = user;
        _createdAt = createdAt;
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
