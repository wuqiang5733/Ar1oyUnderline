package com.example.yora.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.services.entities.Message;
import com.squareup.picasso.Picasso;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    private ImageView _avatar;
    private TextView _displayName;
    private TextView _createdAt;
    private CardView _cardView;
    private TextView _sentReceived;

    public MessageViewHolder(View itemView) {
        super(itemView);
        _cardView = (CardView) itemView;
        _avatar = (ImageView) itemView.findViewById(R.id.list_item_message_avatar);
        _displayName = (TextView) itemView.findViewById(R.id.list_item_message_displayName);
        _createdAt = (TextView) itemView.findViewById(R.id.list_item_message_createdAt);
        _sentReceived = (TextView) itemView.findViewById(R.id.list_item_message_sentReceived);
    }

    public void populate(Context context, Message message) {
        itemView.setTag(message);

        Picasso.with(context)
               .load(message.getOtherUser().getAvatarUrl())
               .into(_avatar);

        String createdAt = DateUtils.formatDateTime(
                context,
                message.getCreatedAt().getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);

        _sentReceived.setText(message.isFromUs() ? "sent " : "received ");
        _displayName.setText(message.getOtherUser().getDisplayName());
        _createdAt.setText(createdAt);

        int colorResourceId;
        if (message.isSelected()) {
            colorResourceId = R.color.list_item_message_background_selected;
            _cardView.setCardElevation(5);
        } else if (message.isRead()) {
            colorResourceId = R.color.list_item_message_background;
            _cardView.setCardElevation(2);
        } else {
            colorResourceId = R.color.list_item_message_background_unread;
            _cardView.setCardElevation(3);
        }

        _cardView.setCardBackgroundColor(context.getResources().getColor(colorResourceId));
    }
}
