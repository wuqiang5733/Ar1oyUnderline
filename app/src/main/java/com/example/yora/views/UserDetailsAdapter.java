package com.example.yora.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;
import com.example.yora.services.entities.UserDetails;
import com.squareup.picasso.Picasso;


public class UserDetailsAdapter extends ArrayAdapter<UserDetails> {
    private final LayoutInflater _inflater;

    public UserDetailsAdapter(BaseActivity activity) {
        super(activity, 0);
        _inflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserDetails details = getItem(position);
        ViewHolder view;

        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.list_item_user_details, parent, false);
            view = new ViewHolder(convertView);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        view.DisplayName.setText(details.getDisplayName());
        Picasso.with(getContext())
                .load(details.getAvatarUrl())
                .into(view.Avatar);

        return convertView;
    }

    private class ViewHolder {
        public TextView DisplayName;
        public ImageView Avatar;

        public ViewHolder(View view) {
            DisplayName = (TextView) view.findViewById(R.id.list_item_user_details_displayName);
            Avatar = (ImageView) view.findViewById(R.id.list_item_user_details_avatar);
        }
    }
}
