package com.example.yora.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yora.R;
import com.example.yora.services.Contacts;
import com.example.yora.services.entities.UserDetails;
import com.example.yora.views.UserDetailsAdapter;
import com.squareup.otto.Subscribe;

public class AddContactActivity extends BaseAuthenticatedActivity implements AdapterView.OnItemClickListener {
    public static final String RESULT_CONTACT = "RESULT_CONTACT";

    private UserDetailsAdapter _adapter;
    private View _progressFrame;
    private Handler _handler;
    private SearchView _searchView;
    private String _lastQuery;
    private UserDetails _selectedUser;

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            _lastQuery = _searchView.getQuery().toString();
            _progressFrame.setVisibility(View.VISIBLE);
            bus.post(new Contacts.SearchUsersRequest(_lastQuery));
        }
    };

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_add_contact);

        _adapter = new UserDetailsAdapter(this);
        ListView listView = (ListView) findViewById(R.id.activity_add_contact_usersListView);
        listView.setOnItemClickListener(this);
        listView.setAdapter(_adapter);

        _progressFrame = findViewById(R.id.activity_add_contact_progressFrame);
        _progressFrame.setVisibility(View.GONE);

        _handler = new Handler();
        _searchView = new SearchView(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(_searchView);

        _searchView.setIconified(false);
        _searchView.setQueryHint("Searhc for users...");
        _searchView.setLayoutParams(new Toolbar.LayoutParams( //Layout information for child views of Toolbars
                Toolbar.LayoutParams.MATCH_PARENT,
                Toolbar.LayoutParams.MATCH_PARENT));

        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() < 3)
                    return true;

                _handler.removeCallbacks(searchRunnable); // Cancel previously queued requests (which have not started processing yet)
                _handler.postDelayed(searchRunnable, 750);
                return true;
            }
        });

        _searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        });
    }

    @Subscribe
    public void onUsersSearched(Contacts.SearchUsersResponse response) {
        _progressFrame.setVisibility(View.GONE);
        if (!response.didSucceed()) {
            response.showErrorToast(this);
            return;
        }

        if (!response.Query.equals(_lastQuery))
            // If this is a response from a previous query not the latest one (stale query)
            // It's possible because the request handler does not respond immediately after it
            // starts processing and in the meantime another request may be sent.
            return;

        _adapter.clear();
        _adapter.addAll(response.Users);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setPositiveButton("Send Contact Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendContactRequest(_adapter.getItem(position));
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void sendContactRequest(UserDetails user) {
        _selectedUser = user;
        _progressFrame.setVisibility(View.VISIBLE);
        bus.post(new Contacts.SendContactRequestRequest(user.getId()));
    }


    @Subscribe
    public void onContactRequestSent(Contacts.SendContactRequestResponse response) {
        _progressFrame.setVisibility(View.GONE);
        if (!response.didSucceed()) {
            response.showErrorToast(this);
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(RESULT_CONTACT, _selectedUser);
        setResult(RESULT_OK, intent);
        finish();
    }
}
