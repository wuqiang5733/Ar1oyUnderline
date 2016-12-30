package com.example.yora.activities;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.services.Messages;
import com.example.yora.services.entities.Message;
import com.example.yora.services.entities.UserDetails;
import com.squareup.otto.Subscribe;

import java.util.GregorianCalendar;

public class MessageActivity extends BaseAuthenticatedActivity implements View.OnClickListener {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    public static final String EXTRA_MESSAGE_ID = "EXTRA_MESSAGE_ID";
    public static final int RESULT_MESSAGE_DELETED = 100;
    public static final String RESULT_EXTRA_MESSAGE_ID = "RESULT_EXTRA_MESSAGE_ID";

    private static final String STATE_MESSAGE = "STATE_MESSAGE";

    private View _drawer;
    private TextView _translateButton;
    private boolean _isOpen;
    private int _translateOffset;
    private AnimatorSet _currentAnimation;
    private Message _currentMessage;
    private View _progressFrame;
    private TextView _shortMessage;
    private TextView _longMessage;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_message);

        _drawer = findViewById(R.id.activity_message_drawer);
        _translateButton = (TextView) findViewById(R.id.activity_message_translate);
        _progressFrame = findViewById(R.id.activity_message_progressFrame);
        _shortMessage = (TextView) findViewById(R.id.activity_message_shortMessage);
        _longMessage = (TextView) findViewById(R.id.activity_message_longMessage);

        _drawer.setOnClickListener(this);

        // Inside layout they have text for preview
        _shortMessage.setText("");
        _longMessage.setText("");

        _progressFrame.setVisibility(View.GONE);

        toolbar.setNavigationIcon(R.drawable.ic_ab_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMessage(RESULT_OK);
            }
        });

        Message message = null;
        if (savedInstanceState != null) {
            message = savedInstanceState.getParcelable(STATE_MESSAGE);
        }

        if (message == null) {
            message = getIntent().getParcelableExtra(EXTRA_MESSAGE);
            if (message == null) {
                int id = getIntent().getIntExtra(EXTRA_MESSAGE_ID, -1);
                if (id != -1) {
                    _progressFrame.setVisibility(View.VISIBLE);
                    bus.post(new Messages.GetMessageDetailsRequest(id));
                } else {
                    message = new Message(
                            1,
                            new GregorianCalendar(),
                            "Short Message",
                            "Long Message",
                            null,
                            new UserDetails(1, true, "Person", "person", ""),
                            false,
                            false);
                }
            }
        }

        if (message != null) {
            showMessage(message);
        }
    }

    @Subscribe
    public void onMessageDetailsLoaded(final Messages.GetMessageDetailsResponse response) {
        scheduler.invokeOnResume(Messages.GetMessageDetailsResponse.class, new Runnable() {
            @Override
            public void run() {
                _progressFrame.setVisibility(View.GONE);
                if (!response.didSucceed()) {
                    response.showErrorToast(MessageActivity.this);
                    return;
                }

                showMessage(response.Message);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MESSAGE, _currentMessage);
    }

    private void showMessage(Message message) {
        _currentMessage = message;

        String createdAt = DateUtils.formatDateTime(this, message.getCreatedAt().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        if (message.isFromUs()) {
            getSupportActionBar().setTitle("Sent on " + createdAt);
        } else {
            getSupportActionBar().setTitle("Received on " + createdAt);
        }
        _longMessage.setText(message.getLongMessage());
        _shortMessage.setText(message.getShortMessage());

        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            // TODO load image
        }

        invalidateOptionsMenu();

        Intent defaultResult = new Intent();
        defaultResult.putExtra(RESULT_EXTRA_MESSAGE_ID, message.getId());
        setResult(RESULT_OK, defaultResult);

        if (!message.isRead()) {
            bus.post(new Messages.MarkMessageAsReadRequest(message.getId()));
        }
    }

    private void closeMessage(int resultCode) {
        Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_MESSAGE_ID, _currentMessage.getId());
        setResult(resultCode, data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_message_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.activity_message_menuContact) {
            Intent intent = new Intent(this, ContactActivity.class);
            intent.putExtra(ContactActivity.EXTRA_USER_DETAILS, _currentMessage.getOtherUser());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.activity_message_menuReply) {
            Intent intent = new Intent(this, NewMessageActivity.class);
            intent.putExtra(NewMessageActivity.EXTRA_CONTACT, _currentMessage.getOtherUser());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.activity_message_menuDelete) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Message?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bus.post(new Messages.DeleteMessageRequest(_currentMessage.getId()));
                            closeMessage(RESULT_MESSAGE_DELETED);
                        }
                    })
                    .setCancelable(false)
                    .setNeutralButton("Cancel", null)
                    .create();
            dialog.show();
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_message_menuReply)
            .setVisible(_currentMessage != null && !_currentMessage.isFromUs());
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        _translateOffset = _drawer.getHeight() - _translateButton.getHeight();
        _drawer.setTranslationY(_translateOffset);
    }

    @Override
    public void onClick(View view) {
        _isOpen = !_isOpen;

        if (_currentAnimation != null) {
            _currentAnimation.cancel();
        }

        int currentBackgroundColor = ((ColorDrawable) _drawer.getBackground()).getColor();
        int translationY, color;

        if (!_isOpen) {
            translationY = 0;
            color = Color.parseColor("#EE1998FC");
            _translateButton.setText("Close");
        } else {
            translationY = _translateOffset;
            color = Color.parseColor("#221998FC");
            _translateButton.setText("Translate");
        }

        ObjectAnimator translateAnimator = ObjectAnimator
                .ofFloat(_drawer, "translationY", translationY)
                .setDuration(100);

        ObjectAnimator colorAnimator = ObjectAnimator
                .ofInt(_drawer, "backgroundColor", currentBackgroundColor, color)
                .setDuration(100);
        colorAnimator.setEvaluator(new ArgbEvaluator());

        _currentAnimation = new AnimatorSet();
        _currentAnimation.setDuration(300);
        _currentAnimation.play(translateAnimator).with(colorAnimator);
        _currentAnimation.start();
    }
}
