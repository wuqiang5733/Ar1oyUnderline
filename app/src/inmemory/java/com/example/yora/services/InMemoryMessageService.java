package com.example.yora.services;

import com.example.yora.infrastructure.YoraApplication;
import com.example.yora.services.entities.Message;
import com.example.yora.services.entities.UserDetails;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;

public class InMemoryMessageService extends BaseInMemoryService {
    public InMemoryMessageService(YoraApplication application) {
        super(application);
    }

    @Subscribe
    public void deleteMessage(Messages.DeleteMessageRequest request) {
        Messages.DeleteMessageResponse response = new Messages.DeleteMessageResponse();
        response.MessageId = request.MessageId;
        postDelayed(response);
    }

    @Subscribe
    public void SearchMessages(Messages.SearchMessagesRequest request) {
        Messages.SeacrhMessagesResponse response = new Messages.SeacrhMessagesResponse();
        response.Messages = new ArrayList<>();

        UserDetails[] users = new UserDetails[10];

        for (int i = 0; i < users.length; i++) {
            String stringId = Integer.toString(i);
            users[i] = new UserDetails(
                    i,
                    true,
                    "User " + stringId,
                    "user_" + stringId,
                    "http://www.gravatar.com/avatar/" + stringId + "?d=identicon");
        }

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, -100);

        for (int i = 0; i < 100; i++) {
            boolean isFromUs;

            if (request.IncludeReceivedMessages && request.IncludeSentMessages) {
                isFromUs = random.nextBoolean();
            } else {
                isFromUs = !request.IncludeReceivedMessages;
            }

            date.set(Calendar.MINUTE, random.nextInt(60 * 24));

            String numberString = Integer.toString(i);
            response.Messages.add(new Message(
                    i,
                    (Calendar) date.clone(), // Calendar is a mutable reference type
                    "Short Message " + numberString,
                    "Long Message " + numberString,
                    "",
                    users[random.nextInt(users.length)],
                    isFromUs,
                    i > 4));
        }

        postDelayed(response, 2000);
    }

    @Subscribe
    public void sendMessage(Messages.SendMessageRequest request) {
        Messages.SendMessageResponse response = new Messages.SendMessageResponse();
        if (request.getMessage().equals("error")) {
            response.setOperationError("Something bad happened");
        } else if (request.getMessage().equals("error-message")) {
            response.setPropertyError("message", "Invalid message");
        }
        postDelayed(response, 1500, 3000);
    }

    @Subscribe
    public void markMessageAsRead(Messages.MarkMessageAsReadRequest request) {
        postDelayed(new Messages.MarkMessageAsReadResponse());
    }

    @Subscribe
    public void getMessageDetails(Messages.GetMessageDetailsRequest request) {
        Messages.GetMessageDetailsResponse response = new Messages.GetMessageDetailsResponse();
        response.Message = new Message(
                1,
                Calendar.getInstance(),
                "Short Message",
                "Long Message",
                null,
                new UserDetails(1, true, "Display Name", "Username", ""),
                false,
                false);

        postDelayed(response);
    }
}
