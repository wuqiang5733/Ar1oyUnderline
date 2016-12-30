package com.example.yora.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.yora.R;
import com.example.yora.services.Contacts;
import com.example.yora.services.Messages;
import com.example.yora.services.entities.Message;
import com.example.yora.services.entities.UserDetails;
import com.example.yora.views.MessagesAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class ContactActivity extends BaseAuthenticatedActivity implements MessagesAdapter.OnMessageClickedListener {
    public static final String EXTRA_USER_DETAILS = "EXTRA_USER_DETAILS";
    public static final int RESULT_USER_REMOVED = 101;

    private static final int REQUEST_VIEW_MESSAGE = 1;
    private static final int REQUEST_SEND_MESSAGE = 2;

    private UserDetails _userDetails;
    private MessagesAdapter _adapter;
    private ArrayList<Message> _messages;
    private View _progressFrame;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contact);
        _progressFrame = findViewById(R.id.activity_contact_progressFrame);

        _userDetails = getIntent().getParcelableExtra(EXTRA_USER_DETAILS);
        if (_userDetails == null) {
            // Just for test purposes (can start running the app from ContactActivity)
            _userDetails = new UserDetails(1, true, "A Contact", "a_contact", "http://www.gravatar.com/avatar/1.jpg");
        }

        getSupportActionBar().setTitle(_userDetails.getDisplayName());
        toolbar.setNavigationIcon(R.drawable.ic_ab_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        _adapter = new MessagesAdapter(this, this);
        _messages = _adapter.getMessages();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_contact_messages);
        recyclerView.setAdapter(_adapter);

        if (isTablet) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        scheduler.postEveryMilliseconds(new Messages.SearchMessagesRequest(_userDetails.getId(), true, true), 1000 * 60 * 3);
    }


    @Override
    public void onMessageClicked(Message message) {
        Intent  intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.EXTRA_MESSAGE, message);
        startActivityForResult(intent, REQUEST_VIEW_MESSAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIEW_MESSAGE && resultCode == MessageActivity.RESULT_MESSAGE_DELETED) {
            int messageId = data.getIntExtra(MessageActivity.RESULT_EXTRA_MESSAGE_ID, -1);
            if (messageId == -1) {
                return;
            }
            for (int i = 0; i < _messages.size(); i++) {
                Message message = _messages.get(i);
                if (message.getId() == messageId) {
                    _messages.remove(i);
                    _adapter.notifyItemRemoved(i);
                    break;
                }
            }
        } else if (requestCode == REQUEST_SEND_MESSAGE && resultCode == RESULT_OK) {
            _progressFrame.setVisibility(View.VISIBLE);
            bus.post(new Messages.SearchMessagesRequest(_userDetails.getId(), true, true));
        }
    }

    @Subscribe
    public void onMessagesReceived(final Messages.SeacrhMessagesResponse response) {
        scheduler.invokeOnResume(Messages.SeacrhMessagesResponse.class, new Runnable() {
            @Override
            public void run() {
                _progressFrame.setVisibility(View.GONE);
                if (!response.didSucceed()) {
                    response.showErrorToast(ContactActivity.this);
                    return;
                }
                int oldMessagesSize = _messages.size();
                _messages.clear();
                _adapter.notifyItemRangeRemoved(0, oldMessagesSize);
                _messages.addAll(response.Messages);
                _adapter.notifyItemRangeInserted(0, _messages.size());
            }
        });
    }

    private void doRemoveContact() {
        _progressFrame.setVisibility(View.VISIBLE);
        bus.post(new Contacts.RemoveContactRequest(_userDetails.getId()));
    }

    @Subscribe
    public void onRemoveContact(final Contacts.RemoveContactResponse response) {
        scheduler.invokeOnResume(Contacts.RemoveContactResponse.class, new Runnable() {
            @Override
            public void run() {
                if (!response.didSucceed()) {
                    response.showErrorToast(ContactActivity.this);
                    _progressFrame.setVisibility(View.GONE);
                    return;
                }
                setResult(RESULT_USER_REMOVED);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.activity_contact_menuNewMessage) {
            Intent intent = new Intent(this, NewMessageActivity.class);
            intent.putExtra(NewMessageActivity.EXTRA_CONTACT, _userDetails);
            startActivityForResult(intent, REQUEST_SEND_MESSAGE);
            return true;
        }

        if (itemId == R.id.activity_contact_menuRemoveContact) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Remove Contact?")
                    .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doRemoveContact();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
            return true;
        }

        return false;
    }
}
