package com.example.yora.views;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;
import com.example.yora.services.entities.ContactRequest;
import com.squareup.picasso.Picasso;

public class ContactRequestAdapter extends ArrayAdapter<ContactRequest> {
    private LayoutInflater _inflater;

    public ContactRequestAdapter(BaseActivity activity) {
        super(activity, 0);
        _inflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactRequest request = getItem(position);
        ViewHolder view;
        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.list_item_contact_request, parent, false);
            view = new ViewHolder(convertView);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        view.DisplayName.setText(request.getUser().getDisplayName());
        Picasso.with(getContext())
               .load(request.getUser().getAvatarUrl())
               .into(view.Avatar);
        String createdAt = DateUtils.formatDateTime(
                getContext(),
                request.getCreatedAt().getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);

        if (request.isFromUs()) {
            view.CreatedAt.setText("Sent at " + createdAt);
        } else {
            view.CreatedAt.setText("Received " + createdAt);
        }

        return convertView;
    }

    private class ViewHolder {
        public TextView DisplayName;
        public TextView CreatedAt;
        public ImageView Avatar;

        public ViewHolder(View view) {
            DisplayName = (TextView) view.findViewById(R.id.list_item_contact_request_displayName);
            CreatedAt = (TextView) view.findViewById(R.id.list_item_contact_request_createdAt);
            Avatar = (ImageView) view.findViewById(R.id.list_item_contact_request_avatar);
        }
    }
}
