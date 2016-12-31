package com.example.yora.services;

import com.example.yora.infrastructure.RetrofitCallbackPost;
import com.example.yora.infrastructure.YoraApplication;
import com.squareup.otto.Subscribe;

public class LiveContactService extends BaseLiveService {
    protected LiveContactService(YoraApplication application, YoraWebService api) {
        super(application, api);
    }

    @Subscribe
    public void searchUsers(Contacts.SearchUsersRequest request) {
        api.searchUsers(request.Query, new RetrofitCallbackPost<>(Contacts.SearchUsersResponse.class, bus));
    }

    @Subscribe
    public void sendContactRequest(Contacts.SendContactRequestRequest request) {
        api.sendContactRequest(request.UserId, new RetrofitCallbackPost<>(Contacts.SendContactRequestResponse.class, bus));
    }

    @Subscribe
    public void getContactRequests(Contacts.GetContactRequestsRequest request) {
        if (request.FromUs) {
            api.getContactRequestsFromUs(new RetrofitCallbackPost<>(Contacts.GetContactRequestsResponse.class, bus));
        } else {
            api.getContactRequestsToUs(new RetrofitCallbackPost<>(Contacts.GetContactRequestsResponse.class, bus));
        }
    }

    @Subscribe
    public void respondToContactRequest(Contacts.RespondToContactRequestRequest request) {
        String response = request.Accept ? "accept" : "reject";
        api.respondToContactRequest(
                request.ContactRequestId,
                new YoraWebService.RespondToContactRequest(response),
                new RetrofitCallbackPost<>(Contacts.RespondToContactRequestResponse.class, bus));
    }

    @Subscribe
    public void getContacts(Contacts.GetContactRequestsRequest request) {
        api.getContacts(new RetrofitCallbackPost<>(Contacts.GetContactsResponse.class, bus));
    }

    @Subscribe
    public void removeContact(Contacts.RemoveContactRequest request) {
        api.removeContact(request.ContactId, new RetrofitCallbackPost<>(Contacts.RemoveContactResponse.class, bus));
    }
}
