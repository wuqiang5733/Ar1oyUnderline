package com.example.yora.services.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDetails implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Creator<UserDetails> CREATOR = new Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel source) {
            return new UserDetails(0, false, null, null, null);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[0];
        }
    };
}
