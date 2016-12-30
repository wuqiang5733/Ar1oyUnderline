package com.example.yora.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;
import com.example.yora.services.entities.Message;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> implements View.OnClickListener {
    private final LayoutInflater _layoutInflater;
    private final BaseActivity _activity;
    private final OnMessageClickedListener _listener;
    private final ArrayList<Message> _messages;

    public MessagesAdapter(BaseActivity activity, OnMessageClickedListener listener) {
        _activity = activity;
        _listener = listener;
        _messages = new ArrayList<>();
        _layoutInflater = activity.getLayoutInflater();
    }

    public ArrayList<Message> getMessages() {
        return _messages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageViewHolder viewHolder = new MessageViewHolder(_layoutInflater, parent);
        viewHolder.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = _messages.get(position);
        holder.populate(_activity, message);
    }

    @Override
    public int getItemCount() {
        return _messages.size();
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof Message) {
            Message message = (Message) view.getTag();
            _listener.onMessageClicked(message);
        }
    }

    public interface OnMessageClickedListener {
        void onMessageClicked(Message message);
    }
}
