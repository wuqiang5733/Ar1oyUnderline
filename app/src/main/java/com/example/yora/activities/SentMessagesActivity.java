package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.yora.R;
import com.example.yora.services.Messages;
import com.example.yora.services.entities.Message;
import com.example.yora.views.MainNavDrawer;
import com.example.yora.views.MessagesAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class SentMessagesActivity extends BaseAuthenticatedActivity implements MessagesAdapter.OnMessageClickedListener {
    private static final int REQUEST_VIEW_MESSAGE = 1;

    private MessagesAdapter _adapter;
    private ArrayList<Message> _messages;
    private View _progressFrame;


    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sent_message);
        setNavDrawer(new MainNavDrawer(this));
        getSupportActionBar().setTitle("Sent Messages");

        _adapter = new MessagesAdapter(this, this);
        _messages = _adapter.getMessages();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_sent_messages_messages);
        recyclerView.setAdapter(_adapter);

        if (isTablet) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        _progressFrame = findViewById(R.id.activity_sent_messages_progressFrame);

        scheduler.postEveryMilliseconds(new Messages.SearchMessagesRequest(true, false), 1000 * 60 * 3 /*3 min*/);
    }

    @Override
    public void onMessageClicked(Message message) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.EXTRA_MESSAGE, message);
        startActivityForResult(intent, REQUEST_VIEW_MESSAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_VIEW_MESSAGE || resultCode != MessageActivity.REQUEST_MESSAGE_DELETED) {
            return;
        }

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
    }

    @Subscribe
    public void onMessagesLoaded(final Messages.SeacrhMessagesResponse response) {
        response.showErrorToast(SentMessagesActivity.this);
        _progressFrame.setVisibility(View.GONE);
        int oldMessagesSize = _messages.size();
        _messages.clear();
        _adapter.notifyItemRangeRemoved(0, oldMessagesSize);
        _messages.addAll(response.Messages);
        _adapter.notifyItemRangeInserted(0, _messages.size());
    }
}
