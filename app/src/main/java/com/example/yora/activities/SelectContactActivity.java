package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yora.R;
import com.example.yora.services.Contacts;
import com.example.yora.services.entities.UserDetails;
import com.example.yora.views.UserDetailsAdapter;
import com.squareup.otto.Subscribe;

public class SelectContactActivity extends BaseAuthenticatedActivity implements AdapterView.OnItemClickListener {
    public static final String RESULT_CONTACT = "RESULT_CONTACT";

    private UserDetailsAdapter _adapter;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_select_contact);
        getSupportActionBar().setTitle("Select Contact");

        _adapter = new UserDetailsAdapter(this);
        ListView listView = (ListView) findViewById(R.id.activity_select_contact_listView);
        listView.setAdapter(_adapter);
        listView.setOnItemClickListener(this);

        bus.post(new Contacts.GetContactsRequest(true));
    }

    @Subscribe
    public void onContactsReceived(Contacts.GetContactsResponse response) {
        response.showErrorToast(this);
        _adapter.clear();
        _adapter.addAll(response.Contacts);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserDetails selectedContact = _adapter.getItem(position);
        Intent intent = new Intent();
        intent.putExtra(RESULT_CONTACT, selectedContact);
        setResult(RESULT_OK, intent);
        finish();
    }
}
