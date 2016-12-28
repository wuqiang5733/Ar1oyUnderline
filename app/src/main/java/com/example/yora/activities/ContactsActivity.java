package com.example.yora.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.yora.R;
import com.example.yora.fragments.ContactsFragment;
import com.example.yora.fragments.PendingContactRequestsFragment;
import com.example.yora.views.MainNavDrawer;

public class ContactsActivity extends BaseAuthenticatedActivity implements AdapterView.OnItemSelectedListener {
    private ObjectAnimator _currentAnimation;
    private ArrayAdapter<ContactsSpinnerItem> _adapter;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contacts);
        setNavDrawer(new MainNavDrawer(this));

        _adapter = new ArrayAdapter<>(this, R.layout.list_item_toolbar_spinner); // The layout used when spinner is not open
        _adapter.setDropDownViewResource(android.R.layout.simple_list_item_1); // The layout used in spinner drop down state
        _adapter.add(new ContactsSpinnerItem("Contacts", Color.parseColor("#00BCD4"), ContactsFragment.class));
        _adapter.add(new ContactsSpinnerItem(
                "Pending Contact Requests",
                getResources().getColor(R.color.contacts_pending_contacts_request), // Just for demonstration
                PendingContactRequestsFragment.class));

        Spinner spinner = (Spinner) findViewById(R.id.activity_contacts_spinner);
        spinner.setAdapter(_adapter);
        spinner.setOnItemSelectedListener(this);

        getSupportActionBar().setTitle(null); // Because we use spinner in place of title
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ContactsSpinnerItem item = _adapter.getItem(position);
        if (item == null)
            return;

        if (_currentAnimation != null)
            _currentAnimation.end();

        int currentColor = ((ColorDrawable) toolbar.getBackground()).getColor();

        _currentAnimation = ObjectAnimator
                .ofObject(toolbar, "backgroundColor", new ArgbEvaluator(), currentColor, item.getColor())
                .setDuration(250);

        _currentAnimation.start();

        Fragment fragment;
        try {
            fragment = (Fragment) item.getFragment().newInstance();
        }
        catch (Exception e) {
            Log.e("ContactsActivity", "Could not instantiate fragment" + item.getFragment().getName(), e);
            return;
        }

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.activity_contacts_fragment_container, fragment)
                .commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class ContactsSpinnerItem {
        private final String title;
        private final int color; // The color we switch to when switching fragment
        private final Class fragment; // Reference to the actual fragment we switch to

        public ContactsSpinnerItem(String title, int color, Class fragment) {
            this.title = title;
            this.color = color;
            this.fragment = fragment;
        }

        public String getTitle() {
            return title;
        }

        public int getColor() {
            return color;
        }

        public Class getFragment() {
            return fragment;
        }

        @Override
        public String toString() {
            return getTitle();
        }
    }
}
