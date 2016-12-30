package com.example.yora.services;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.yora.infrastructure.ServiceResponse;
import com.example.yora.services.entities.Message;
import com.example.yora.services.entities.UserDetails;

import java.util.List;

public final class Messages {
    private Messages() {
    }

    public static class DeleteMessageRequest {
        public int MessageId;

        public DeleteMessageRequest(int messageId) {
            MessageId = messageId;
        }
    }

    public static class DeleteMessageResponse extends ServiceResponse {
        public int MessageId;
    }

    public static class SearchMessagesRequest {
        public int FromContactId;
        public boolean IncludeSentMessages;
        public boolean IncludeReceivedMessages;

        public SearchMessagesRequest(int fromContactId, boolean includeSentMessages, boolean includeReceivedMessages) {
            FromContactId = fromContactId;
            IncludeSentMessages = includeSentMessages;
            IncludeReceivedMessages = includeReceivedMessages;
        }

        public SearchMessagesRequest(boolean includeSentMessages, boolean includeReceivedMessages) {
            FromContactId = -1;
            IncludeSentMessages = includeSentMessages;
            IncludeReceivedMessages = includeReceivedMessages;
        }
    }

    public static class SeacrhMessagesResponse extends ServiceResponse {
        public List<Message> Messages;
    }

    public static class SendMessageRequest implements Parcelable {
        private UserDetails _recipient;
        private Uri _imagePath;
        private String _message;

        public SendMessageRequest() {
        }

        private SendMessageRequest(Parcel in) {
            _recipient = in.readParcelable(UserDetails.class.getClassLoader());
            _imagePath = in.readParcelable(Uri.class.getClassLoader());
            _message = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(_recipient, 0);
            out.writeParcelable(_imagePath, 0);
            out.writeString(_message);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public UserDetails getRecipient() {
            return _recipient;
        }

        public void setRecipient(UserDetails recipient) {
            _recipient = recipient;
        }

        public Uri getImagePath() {
            return _imagePath;
        }

        public void setImagePath(Uri imagePath) {
            _imagePath = imagePath;
        }

        public String getMessage() {
            return _message;
        }

        public void setMessage(String message) {
            _message = message;
        }

        public static Creator<SendMessageRequest> CREATOR = new Creator<SendMessageRequest>() {
            @Override
            public SendMessageRequest createFromParcel(Parcel source) {
                return new SendMessageRequest(source);
            }

            @Override
            public SendMessageRequest[] newArray(int size) {
                return new SendMessageRequest[size];
            }
        };
    }


    public static class SendMessageResponse extends ServiceResponse {
        public Message Message;
    }

    public static class MarkMessageAsReadRequest {
        public int MessageId;

        public MarkMessageAsReadRequest(int messageId) {
            MessageId = messageId;
        }
    }

    public static class MarkMessageAsReadResponse extends ServiceResponse {
    }

    public static class GetMessageDetailsRequest {
        public int Id;

        public GetMessageDetailsRequest(int id) {
            Id = id;
        }
    }

    public static class GetMessageDetailsResponse extends ServiceResponse {
        public Message Message;
    }
}
