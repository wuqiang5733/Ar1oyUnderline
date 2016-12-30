package com.example.yora.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.yora.R;
import com.example.yora.activities.BaseActivity;
import com.example.yora.services.Contacts;
import com.example.yora.views.ContactRequestAdapter;
import com.squareup.otto.Subscribe;

public class PendingContactRequestsFragment extends BaseFragment {
    private View _progressFrame;
    private ContactRequestAdapter _adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_contact_requests, container, false);
        _progressFrame = view.findViewById(R.id.fragment_pending_contact_requests_progressFrame);
        _adapter = new ContactRequestAdapter((BaseActivity) getActivity());

        ListView listView = (ListView) view.findViewById(R.id.fragment_pending_contact_requests_list);
        listView.setAdapter(_adapter);

        bus.post(new Contacts.GetContactRequestsRequest(true));

        return view;
    }

    @Subscribe
    public void onGetContactRequests(final Contacts.GetContactRequestsResponse response) {
        scheduler.invokeOnResume(Contacts.GetContactRequestsResponse.class, new Runnable() {
            @Override
            public void run() {
                _progressFrame.animate()
                        .alpha(0)
                        .setDuration(250)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                _progressFrame.setVisibility(View.GONE);
                            }
                        })
                        .start();

                if (!response.didSucceed()) {
                    response.showErrorToast(getActivity());
                    return;
                }

                _adapter.clear();
                _adapter.addAll(response.Requests);
            }
        });

    }
}
