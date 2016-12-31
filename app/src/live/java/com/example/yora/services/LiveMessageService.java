package com.example.yora.services;

import com.example.yora.infrastructure.RetrofitCallbackPost;
import com.example.yora.infrastructure.YoraApplication;
import com.squareup.otto.Subscribe;

import java.io.File;

import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public class LiveMessageService extends BaseLiveService {
    protected LiveMessageService(YoraApplication application, YoraWebService api) {
        super(application, api);
    }

    @Subscribe
    public void SendMessage(Messages.SendMessageRequest request) {
        api.sendMessage(
                new TypedString(request.getMessage()),
                new TypedString(Integer.toString(request.getRecipient().getId())),
                new TypedFile("image/jpeg", new File(request.getImagePath().getPath())),
                new RetrofitCallbackPost<>(Messages.SendMessageResponse.class, bus));
    }

    @Subscribe
    public void searchMessages(Messages.SearchMessagesRequest request) {
        if (request.FromContactId != -1) {
            api.searchMessages(
                    request.FromContactId,
                    request.IncludeSentMessages,
                    request.IncludeReceivedMessages,
                    new RetrofitCallbackPost<>(Messages.SeacrhMessagesResponse.class, bus));
        } else {
            api.searchMessages(
                    request.IncludeSentMessages,
                    request.IncludeReceivedMessages,
                    new RetrofitCallbackPost<>(Messages.SeacrhMessagesResponse.class, bus));
        }
    }

    @Subscribe
    public void deleteMessage(Messages.DeleteMessageRequest request) {
        api.deleteMessage(request.MessageId, new RetrofitCallbackPost<>(Messages.DeleteMessageResponse.class, bus));
    }

    @Subscribe
    public void markMessageAsRead(Messages.MarkMessageAsReadRequest request) {
        api.markMessageAsRead(request.MessageId, new RetrofitCallbackPost<>(Messages.MarkMessageAsReadResponse.class, bus));
    }

    @Subscribe
    public void getMessageDetails(Messages.GetMessageDetailsRequest request) {
        api.getMessageDetails(request.Id, new RetrofitCallbackPost<>(Messages.GetMessageDetailsResponse.class, bus));
    }
}
