package com.example.yora.services.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Message implements Parcelable {
    private int _id;
    private Calendar _createdAt;
    private String _shortMessage;
    private String _longMessage;
    private String _imageUrl;
    private UserDetails _otherUser;
    private boolean _isFromUs;
    private boolean _isRead;
    private boolean _isSelected;

    public Message(int id, Calendar createdAt, String shortMessage, String longMessage, String imageUrl, UserDetails otherUser, boolean isFromUs, boolean isRead) {
        _id = id;
        _createdAt = createdAt;
        _shortMessage = shortMessage;
        _longMessage = longMessage;
        _imageUrl = imageUrl;
        _otherUser = otherUser;
        _isFromUs = isFromUs;
        _isRead = isRead;
    }

    private Message(Parcel source) {
        _id = source.readInt();
        _createdAt = new GregorianCalendar();
        _createdAt.setTimeInMillis(source.readLong());
        _shortMessage = source.readString();
        _longMessage = source.readString();
        _imageUrl = source.readString();
        _otherUser = source.readParcelable(UserDetails.class.getClassLoader());
        _isFromUs = source.readByte() == 1;
        _isRead = source.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeLong(_createdAt.getTimeInMillis());
        dest.writeString(_shortMessage);
        dest.writeString(_longMessage);
        dest.writeString(_imageUrl);
        dest.writeParcelable(_otherUser, 0);
        dest.writeByte((byte) (_isFromUs ? 1 : 0));
        dest.writeByte((byte) (_isRead ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public int getId() {
        return _id;
    }

    public Calendar getCreatedAt() {
        return _createdAt;
    }

    public String getShortMessage() {
        return _shortMessage;
    }

    public String getLongMessage() {
        return _longMessage;
    }

    public String getImageUrl() {
        return _imageUrl;
    }

    public UserDetails getOtherUser() {
        return _otherUser;
    }

    public boolean isFromUs() {
        return _isFromUs;
    }

    public boolean isRead() {
        return _isRead;
    }

    public void setRead(boolean read) {
        _isRead = read;
    }

    public boolean isSelected() {
        return _isSelected;
    }

    public void setSelected(boolean selected) {
        _isSelected = selected;
    }
}
