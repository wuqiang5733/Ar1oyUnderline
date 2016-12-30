package com.example.yora.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.services.Contacts;
import com.example.yora.services.Messages;
import com.example.yora.services.entities.ContactRequest;
import com.example.yora.services.entities.Message;
import com.example.yora.services.entities.UserDetails;
import com.example.yora.views.MainActivityAdapter;
import com.example.yora.views.MainNavDrawer;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends BaseAuthenticatedActivity implements View.OnClickListener, MainActivityAdapter.MainActivityListener {
    private static final int REQUEST_SHOW_MESSAGE = 1;

    private MainActivityAdapter _adapter;
    private List<Message> _messages;
    private List<ContactRequest> _contactRequests;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Inbox");
        setNavDrawer(new MainNavDrawer(this));

        findViewById(R.id.activity_main_newMessageButton).setOnClickListener(this);

        _adapter = new MainActivityAdapter(this, this);
        _messages = _adapter.getMessages();
        _contactRequests = _adapter.getContactRequests();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerView);
        recyclerView.setAdapter(_adapter);
        if (isTablet) {
            GridLayoutManager manager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(manager);
            // In order to make headers span on two columns
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position == 0) {
                        return 2;
                    }
                    if (_contactRequests.size() > 0 && position == _contactRequests.size() + 1) {
                        return 2;
                    }
                    return 1;
                }
            });
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        scheduler.invokeEveryMilliseconds(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        }, 1000 * 60 * 3);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.activity_main_newMessageButton) {
            startActivity(new Intent(this, NewMessageActivity.class));
        }
    }

    @Override
    public void onRefresh() {
        swipeRefresh.setRefreshing(true);
        bus.post(new Messages.SearchMessagesRequest(false, true));
        bus.post(new Contacts.GetContactRequestsRequest(false));
    }

    @Subscribe
    public void onMessagesLoaded(final Messages.SeacrhMessagesResponse response) {
        scheduler.invokeOnResume(Messages.SeacrhMessagesResponse.class, new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(false);
                if (!response.didSucceed()) {
                    response.showErrorToast(MainActivity.this);
                    return;
                }

                _messages.clear();
                _messages.addAll(response.Messages);
                _adapter.notifyDataSetChanged();
            }
        });
    }

    @Subscribe
    public void onContactRequestsLoaded(final Contacts.GetContactRequestsResponse response) {
        scheduler.invokeOnResume(Messages.SeacrhMessagesResponse.class, new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(false);
                if (!response.didSucceed()) {
                    response.showErrorToast(MainActivity.this);
                    return;
                }

                _contactRequests.clear();
                _contactRequests.addAll(response.Requests);
                _adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onMessageClicked(Message message) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.EXTRA_MESSAGE, message);
        startActivityForResult(intent, REQUEST_SHOW_MESSAGE);
    }

    @Override
    public void onContactRequestClicked(final ContactRequest request, final int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_display, null);
        ImageView avatar = (ImageView) dialogView.findViewById(R.id.dialog_user_display_avatar);
        TextView displayName = (TextView) dialogView.findViewById(R.id.dialog_user_display_displayName);

        UserDetails user = request.getUser();
        displayName.setText(user.getDisplayName());
        Picasso.with(this)
               .load(user.getAvatarUrl())
               .into(avatar);

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_NEUTRAL)
                    return;

                boolean doAccept = which == dialog.BUTTON_POSITIVE;
                _contactRequests.remove(request);
                _adapter.notifyItemRemoved(position + 1); // count the header too

                if (_contactRequests.size() == 0) {
                    _adapter.notifyItemRemoved(0);
                }

                bus.post(new Contacts.RespondToContactRequestRequest(request.getUser().getId(), doAccept));
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Respond to Contact Request")
                .setView(dialogView)
                .setPositiveButton("Accept", clickListener)
                .setNeutralButton("Cancel", clickListener)
                .setNegativeButton("Reject", clickListener)
                .setCancelable(false)
                .create();

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHOW_MESSAGE) {
            int messageId = data.getIntExtra(MessageActivity.RESULT_EXTRA_MESSAGE_ID, -1);
            if (messageId == -1)
                return;

            for (int i = 0; i < _messages.size(); i++) {
                Message message = _messages.get(i);
                if (message.getId() == messageId) {
                    if (resultCode == MessageActivity.RESULT_MESSAGE_DELETED) {
                        _messages.remove(message);
                    } else {
                        message.setRead(true);
                    }
                    _adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}
