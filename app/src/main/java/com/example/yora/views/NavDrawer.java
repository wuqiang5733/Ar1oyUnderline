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

//import android.support.v4.widget.DrawerLayout;

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
        // drawer_layout ： Inbox , Send Message , Contacts  , Profile 各有一个 drawer_layout
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout); // 每个主页的界面
        navDrawerView = (ViewGroup) activity.findViewById(R.id.nav_drawer);// 整个抽出来的那个界面
        // navDrawerView : 整个抽出来的那个界面
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
    }  // 这是大类的 构造函数
    /*
    * 在每一个菜单当中都有这样一句： setNavDrawer(new MainNavDrawer(this));
    * 在 MainNavDrawer 当中有这样的句子： addItem(new ActivityNavDrawerItem( MainActivity.class, "Inbox", ..... )
    */
    public void addItem(NavDrawerItem item) {
        _items.add(item);
        // 注意下面这个特殊的用法
        // this 是指 ： protected NavDrawer navDrawer;
        item.navDrawer = this;
        // this 是指 ： protected NavDrawer navDrawer;
    }

    public boolean isOpen() {
        return drawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    public void setOpen(boolean isOpen) {
        if (isOpen)
            drawerLayout.openDrawer(Gravity.LEFT);
        else
            drawerLayout.closeDrawer(Gravity.LEFT);
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
        //navDrawerView : 整个抽出来的那个界面
        public abstract void inflate(LayoutInflater inflater, ViewGroup navDrawerView);
        public abstract void setSelected(boolean isSelected);
    }

    public static class BasicNavDrawerItem extends NavDrawerItem implements View.OnClickListener {
        //        显示 text 或者 badge (如果有的话) ,还有 icon
        private String _text;
        private String _badge;
        private int _iconDrawable;
        private int _containerId; // 放置 Inbox , Send Message , Contacts  , Profile 那四个位置

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
        // navDrawerView : 整个抽出来的那个界面，由父类传过来
        // _containerId ： 放置 Inbox , Send Message , Contacts  , Profile 那四个位置
        // R.layout.list_item_nave_drawer : Inbox , Send Message , Contacts  , Profile 那四个菜单上的元素
        /*
        *  整个这个 inflate 的作用就是：在整个抽出来的界面上，找到菜单应该在的位置，然后把菜单放上去
        *  在 ViewGroup container 的某个地方Inflate自己，并且根据自己是还被选中，改变 Appearance
        *  并且做好 icon , textView , badgeTextView 在横向上的对应
        */
        public void inflate(LayoutInflater inflater, ViewGroup navDrawerView) {
            ViewGroup container = (ViewGroup) navDrawerView.findViewById(_containerId);
            if (container == null)
                throw new RuntimeException("Nav drawer item " + _text + " could not be attached to ViewGroup. View not found.");
            //  做好 icon , textView , badgeTextView 在横向上的对应
            // 下一句，如果不加最后的false，返回的View是包括每个Item的整体。
            // 加了false就是每个Item，所以得用addView添加到container里面。
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
