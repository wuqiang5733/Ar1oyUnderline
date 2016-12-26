package com.example.yora.services.entities;

public class UserDetails {
    private int _id;
    private boolean _isContact;
    private String _displayName;
    private String _username;
    private String _avatarUrl;

    public UserDetails(int id, boolean isContact, String displayName, String username, String avatarUrl) {
        _id = id;
        _isContact = isContact;
        _displayName = displayName;
        _username = username;
        _avatarUrl = avatarUrl;
    }

    public int getId() {
        return _id;
    }

    public boolean isContact() {
        return _isContact;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public String getUsername() {
        return _username;
    }

    public String getAvatarUrl() {
        return _avatarUrl;
    }
}
