package com.example.yora.services;

import com.squareup.otto.Subscribe;
import com.example.yora.infrastructure.YoraApplication;
import com.example.yora.services.entities.ContactRequest;
import com.example.yora.services.entities.UserDetails;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class InMemoryContactService extends BaseInMemoryService {
    public InMemoryContactService(YoraApplication application) {
        super(application);
    }

    @Subscribe
    public void getContactRequests(Contacts.GetContactRequestsRequest request) {
        Contacts.GetContactRequestsResponse response = new Contacts.GetContactRequestsResponse();
        response.Requests = new ArrayList<>();
        for (int i=0; i< 3; i++) {
            response.Requests.add(new ContactRequest(i, request.FromUs, createFakeUser(i,false), new GregorianCalendar()));
        }
        postDelayed(response);
    }

    @Subscribe
    public void getContacts(Contacts.GetContactsRequest request){
        Contacts.GetContactsResponse response =  new Contacts.GetContactsResponse();
        response.Contacts = new ArrayList<>();
        for (int i=0;i <10; i++){
            response.Contacts.add(createFakeUser(i,true));
        }
        postDelayed(response);
    }

    @Subscribe
    public void sendContactRequest(Contacts.SendContactRequestRequest request) {
        Contacts.SendContactRequestResponse response = new Contacts.SendContactRequestResponse();
        if (request.UserId==2) {
            response.setOperationError("Something bad happened!");
        }
        postDelayed(response);
    }

    @Subscribe
    public void respondToContactsRequest(Contacts.RespondToContactRequestRequest request){
        postDelayed(new Contacts.RespondToContactRequestResponse());
    }

    @Subscribe
    public void removeContact(Contacts.RemoveContactRequest request) {
        Contacts.RemoveContactResponse response = new Contacts.RemoveContactResponse();
        response.RemovedContactId = request.ContactId;
        postDelayed(response);
    }

    @Subscribe
    public void searchUsers(Contacts.SearchUsersRequest request) {
        Contacts.SearchUsersResponse response = new Contacts.SearchUsersResponse();
        response.Query = request.Query;
        response.Users = new ArrayList<>();

        for (int i =0; i < request.Query.length(); i++) {
            response.Users.add(createFakeUser(i , false));
        }

        postDelayed(response, 2000, 3000);
    }

    private UserDetails createFakeUser(int id, boolean isContact) {
        String idString = Integer.toString(id);
        return new UserDetails(
                id,
                isContact,
                "Contact " + idString,
                "Contact" + idString,
                "http://www.gravatar.com/avatar/" + idString + "?d=identicon&s=64"
        );
    }
}
