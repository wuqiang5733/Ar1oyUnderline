package com.example.yora.views;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;

import java.util.ArrayList;

public class NavDrawer {
    private ArrayList<NavDrawerItem> _items;
    private NavDrawerItem _selectedItem;

    protected BaseActivity activity;
    protected DrawerLayout drawerLayout;
    protected ViewGroup navDrawerView;

    public NavDrawer(BaseActivity activity){
        this.activity = activity;
        _items = new ArrayList<>();
        //Conventions: There should be a DrawerLayout with id drawer_layout and a ViewGroup with id nav_drawer
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        navDrawerView = (ViewGroup) activity.findViewById(R.id.nav_drawer);

        if (drawerLayout == null || navDrawerView == null)
            throw new RuntimeException("To use this class, you must have views with the ids of drawer_layout and nav_drawer");

        Toolbar toolbar = activity.getToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOpen(!isOpen());
            }
        });

        activity.getYoraApplication().getBus().register(this);
    }

    public void addItem(NavDrawerItem item) {
        _items.add(item);
        item.navDrawer = this;
    }

    public boolean isOpen() {
        return drawerLayout.isDrawerOpen(Gravity.START);
    }

    public void setOpen(boolean isOpen) {
        if (isOpen)
            drawerLayout.openDrawer(Gravity.START);
        else
            drawerLayout.closeDrawer(Gravity.START);
    }

    public void setSelectedItem(NavDrawerItem item){
        if (_selectedItem != null)
            _selectedItem.setSelected(false);

        _selectedItem = item;
        _selectedItem.setSelected(true);
    }

    public void create() {
        LayoutInflater inflater = activity.getLayoutInflater();
        for (NavDrawerItem item : _items) {
            item.inflate(inflater, navDrawerView);
        }
    }

    public void destroy() {
        activity.getYoraApplication().getBus().unregister(this);
    }

    public static abstract class NavDrawerItem {
        protected NavDrawer navDrawer;

        public abstract void inflate(LayoutInflater inflater, ViewGroup navDrawerView);
        public abstract void setSelected(boolean isSelected);
    }

    public static class BasicNavDrawerItem extends NavDrawerItem implements View.OnClickListener {
        private String _text;
        private String _badge;
        private int _iconDrawable;
        private int _containerId;

        private ImageView _icon;
        private TextView _textView;
        private TextView _badgeTextView;
        private View _view;
        private int _defaultTextColor;

        public BasicNavDrawerItem(String text, String badge, int iconDrawable, int containerId) {
            _text = text;
            _badge = badge;
            _iconDrawable = iconDrawable;
            _containerId = containerId;
        }

        @Override
        public void inflate(LayoutInflater inflater, ViewGroup navDrawerView) {
            ViewGroup container = (ViewGroup) navDrawerView.findViewById(_containerId);
            if (container == null)
                throw new RuntimeException("Nav drawer item " + _text + " could not be attached to ViewGroup. View not found.");

            _view = inflater.inflate(R.layout.list_item_nave_drawer, container, false);
            container.addView(_view);
            _view.setOnClickListener(this);

            _icon = (ImageView) _view.findViewById(R.id.list_item_nav_drawer_icon);
            _textView = (TextView) _view.findViewById(R.id.list_item_nav_drawer_text);
            _badgeTextView = (TextView) _view.findViewById(R.id.list_item_nav_drawer_badge);
            _defaultTextColor = _textView.getCurrentTextColor();

            _icon.setImageResource(_iconDrawable);
            _textView.setText(_text);
            if (_badge != null)
                _badgeTextView.setText(_badge);
            else
                _badgeTextView.setVisibility(View.GONE);
        }

        @Override
        public void setSelected(boolean isSelected) {
            if (isSelected) {
                _view.setBackgroundResource(R.drawable.list_item_nav_drawer_selected_item_background);
                _textView.setTextColor(navDrawer.activity.getResources().getColor(R.color.list_item_nav_drawer_selected_item_text_color));
            }
            else {
                _view.setBackground(null);
                _textView.setTextColor(_defaultTextColor);
            }
        }

        @Override
        public void onClick(View v) {
            navDrawer.setSelectedItem(this);
        }

        public void setText(String text) {
            _text = text;
            if (_view != null) {
                _textView.setText(_text);
            }
        }

        public void setBadge(String badge) {
            _badge = badge;
            if (_view != null) {
                if (_badge != null)
                    _badgeTextView.setVisibility(View.VISIBLE);
                else
                    _badgeTextView.setVisibility(View.GONE);
                _badgeTextView.setText(_badge);
            }
        }

        public void setIconDrawable(int iconDrawable) {
            _iconDrawable = iconDrawable;
            if (_view != null) {
                _icon.setImageResource(iconDrawable);
            }
        }
    }

    public static class ActivityNavDrawerItem extends BasicNavDrawerItem {
        private final Class _targetActivity;

        public ActivityNavDrawerItem(Class targetActivity, String text, String badge, int iconDrawable, int containerId) {
            super(text, badge, iconDrawable, containerId);
            _targetActivity = targetActivity;
        }

        @Override
        public void inflate(LayoutInflater inflater, ViewGroup navDrawerView) {
            super.inflate(inflater, navDrawerView);

            if (navDrawer.activity.getClass() == _targetActivity)
                navDrawer.setSelectedItem(this);
        }

        @Override
        public void onClick(View v) {
            navDrawer.setOpen(false);
            final BaseActivity activity = navDrawer.activity;

            if (activity.getClass() == _targetActivity)
                return;

            super.onClick(v);

            /*activity.startActivity(new Intent(activity, _targetActivity));
            activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_out_left);
            activity.finish();*/

            activity.fadeOut(new BaseActivity.FadeOutListener() {
                @Override
                public void onFadeOutEnd() {
                    activity.startActivity(new Intent(activity, _targetActivity));
                    activity.finish();
                }
            });
        }
    }
}
