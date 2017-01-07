package com.example.yora.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;
import com.example.yora.activities.ContactsActivity;
import com.example.yora.activities.MainActivity;
import com.example.yora.activities.ProfileActivity;
import com.example.yora.activities.SentMessagesActivity;
import com.example.yora.infrastructure.User;
import com.example.yora.services.Account;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
/*
* 在每一个菜单当中都有这样一句： setNavDrawer(new MainNavDrawer(this));
* 在 MainNavDrawer 当中有这样的句子： addItem(new ActivityNavDrawerItem( MainActivity.class, "Inbox", ..... )
*/
public class MainNavDrawer extends NavDrawer {
    private final TextView _displayNameText; // NavDrawer 上面的 头像与头像旁边的用户名之类的信息
    private final ImageView _avatarImage;

    public MainNavDrawer(final BaseActivity activity) {
        super(activity);
        //在每一个主菜单当中都有这样一句： setNavDrawer(new MainNavDrawer(this));
        addItem(new ActivityNavDrawerItem(MainActivity.class, "Inbox", null, R.drawable.ic_action_unread, R.id.include_main_nav_drawer_topItems));
        addItem(new ActivityNavDrawerItem(SentMessagesActivity.class, "Sent Messages", null, R.drawable.ic_action_send_now, R.id.include_main_nav_drawer_topItems));
        addItem(new ActivityNavDrawerItem(ContactsActivity.class, "Contacts", null, R.drawable.ic_action_group, R.id.include_main_nav_drawer_topItems));
        addItem(new ActivityNavDrawerItem(ProfileActivity.class, "Profile", null, R.drawable.ic_action_person, R.id.include_main_nav_drawer_topItems));

        addItem(new BasicNavDrawerItem("Logout", null, R.drawable.ic_action_backspace, R.id.include_main_nav_drawer_bottomItems) {
            @Override
            public void onClick(View view) {
                activity.getYoraApplication().getAuth().logout();
                navDrawer.setOpen(false);
            }
        });

        _displayNameText = (TextView) navDrawerView.findViewById(R.id.include_main_nav_drawer_displayName);
        _avatarImage = (ImageView) navDrawerView.findViewById(R.id.include_main_nav_drawer_avatar);

        // getYoraApplication() 在 BaseActivity 当中实现
        User loggedInUser = activity.getYoraApplication().getAuth().getUser();
        // 这个文字可以在 LoginFragment 当中的 onClick 当中设置
        // 从 User 类当中提取文字，然后在 NavDrawer 上面显示
        _displayNameText.setText(loggedInUser.getDisplayName());

        Picasso.with(activity)
               .load(loggedInUser.getAvatarUrl())
               .into(_avatarImage);
    }

    @Subscribe
    public void UserDetailsUpdated(Account.UserDetailsUpdatedEvent event){
        Picasso.with(activity)
                .load(event.User.getAvatarUrl())
                .into(_avatarImage);
        _displayNameText.setText(event.User.getDisplayName());
    }
}
