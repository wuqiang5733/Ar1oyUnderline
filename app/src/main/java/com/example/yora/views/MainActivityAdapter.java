package com.example.yora.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;
import com.example.yora.services.entities.ContactRequest;
import com.example.yora.services.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class MainActivityAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE = 1;
    private static final int VIEW_TYPE_CONTACT_REQUEST = 2;
    private static final int VIEW_TYPE_HEADER = 3;

    private List<Message> _messages;
    private List<ContactRequest> _contactRequests;
    private BaseActivity _activity;
    private LayoutInflater _layoutInflater;
    private MainActivityListener _listener;

    public MainActivityAdapter(BaseActivity activity, MainActivityListener listener) {
        _activity = activity;
        _listener = listener;
        _layoutInflater = _activity.getLayoutInflater();
        _messages = new ArrayList<>();
        _contactRequests = new ArrayList<>();
    }

    public List<Message> getMessages() {
        return _messages;
    }

    public List<ContactRequest> getContactRequests() {
        return _contactRequests;
    }

    @Override
    public int getItemViewType(int position) {
        if (_contactRequests.size() > 0) {
            if (position == 0)
                return VIEW_TYPE_HEADER;

            position--;
            if (position < _contactRequests.size())
                return VIEW_TYPE_CONTACT_REQUEST;

            position -= _contactRequests.size();
        }

        if (_messages.size() > 0) {
            if (position == 0)
                return VIEW_TYPE_HEADER;

            position--;
            if (position < _messages.size())
                return VIEW_TYPE_MESSAGE;

            position -= _messages.size();
        }

        throw new IllegalArgumentException(
                "We are being asked for an item type from position " + position + ", though we have no such item");

        /*
            0   HEADER - Received Contact Requests
            1   CONTACT REQUEST - From user 1
            2   CONTACT REQUEST - From user 2
            3   CONTACT REQUEST - From user 3
            4   CONTACT REQUEST - From user 4
            5   HEADER - Received Messages
            6   MESSAGE - Message 1
            7   MESSAGE - Message 2
            8   MESSAGE - Message 3
            9   MESSAGE - Message 4
         */
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MESSAGE) {
            final MessageViewHolder viewHolder = new MessageViewHolder(_layoutInflater, parent);
            viewHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _listener.onMessageClicked((Message) view.getTag());
                }
            });
            return viewHolder;
        }

        if (viewType == VIEW_TYPE_CONTACT_REQUEST) {
            final ContactRequestViewHolder viewHolder = new ContactRequestViewHolder(_layoutInflater, parent);
            viewHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContactRequest request = (ContactRequest) view.getTag();
                    _listener.onContactRequestClicked(request, _contactRequests.indexOf(request));
                }
            });
            return viewHolder;
        }

        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(_layoutInflater, parent);
        }

        throw new IllegalArgumentException("ViewType " + viewType + " is not supported");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactRequestViewHolder) {
            position--;
            ContactRequest request = _contactRequests.get(position);
            ((ContactRequestViewHolder) holder).populate(_activity, request);
        } else if (holder instanceof MessageViewHolder) {
            position--;
            if (_contactRequests.size() > 0)
                position = position - 1 - _contactRequests.size();
            Message message = _messages.get(position);
            ((MessageViewHolder) holder).populate(_activity, message);
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            if (position == 0 && _contactRequests.size() > 0)
                viewHolder.populate("Received Contact Requests");
            else
                viewHolder.populate("Received Messages");
        } else {
            throw new IllegalArgumentException("Cannot populate holder of type " + holder.getClass().getName());
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (_contactRequests.size() > 0)
            count += 1 + _contactRequests.size();
        if (_messages.size() > 0)
            count += 1 + _messages.size();
        return count;
    }

    public interface MainActivityListener {
        void onMessageClicked(Message message);
        void onContactRequestClicked(ContactRequest request, int position);
    }
}
