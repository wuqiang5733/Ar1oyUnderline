package com.example.yora.services.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDetails implements Parcelable {
    private final int _id;
    private final boolean _isContact;
    private final String _displayName;
    private final String _username;
    private final String _avatarUrl;

    public UserDetails(int id, boolean isContact, String displayName, String username, String avatarUrl) {
        _id = id;
        _isContact = isContact;
        _displayName = displayName;
        _username = username;
        _avatarUrl = avatarUrl;
    }

    private UserDetails(Parcel source) {
        _id = source.readInt();
        _isContact = source.readByte() == 1;
        _displayName = source.readString();
        _username = source.readString();
        _avatarUrl = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // The order should be the same in writeToParcel and createFromParcel
        dest.writeInt(_id);
        dest.writeByte((byte) (_isContact ? 1 : 0));
        dest.writeString(_displayName);
        dest.writeString(_username);
        dest.writeString(_avatarUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserDetails> CREATOR = new Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel source) {
            return new UserDetails(source);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };

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
