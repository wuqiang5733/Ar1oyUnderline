package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.yora.R;
import com.example.yora.services.Contacts;
import com.example.yora.services.Messages;
import com.example.yora.services.entities.ContactRequest;
import com.example.yora.services.entities.Message;
import com.example.yora.views.MainActivityAdapter;
import com.example.yora.views.MainNavDrawer;
import com.squareup.otto.Subscribe;

import java.util.List;

public class MainActivity extends BaseAuthenticatedActivity implements View.OnClickListener, MainActivityAdapter.MainActivityListener {
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
            public void run() {  // 自动刷新
                onRefresh();
            }
        }, 1000 * 60 * 3);  // 每三分钟
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

    }

    @Override
    public void onContactRequestClicked(ContactRequest request, int position) {

    }
}
