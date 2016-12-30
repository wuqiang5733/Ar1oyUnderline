package com.example.yora.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.yora.R;
import com.example.yora.services.Messages;
import com.example.yora.services.entities.UserDetails;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class SendMessageActivity extends BaseAuthenticatedActivity implements View.OnClickListener {
    public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";
    public static final String EXTRA_CONTACT = "EXTRA_CONTACT";
    public static final String RESULT_MESSAGE = "RESULT_MESSAGE";

    public static final int MAX_IMAGE_HEIGHT = 1000;

    private static final String STATE_REQUEST = "STATE_REQUEST";
    private static final int REQUEST_SELECT_RECIPIENT = 1;

    private Messages.SendMessageRequest _request;
    private EditText _messageEditText;
    private Button _recipientButton;
    private View _progressFrame;

    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_send_message);
        getSupportActionBar().setTitle("Send Message");

        if (savedInstanceState != null) {
            _request = savedInstanceState.getParcelable(STATE_REQUEST);
        }

        Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_PATH);
        if (imageUri != null) {
            ImageView image = (ImageView) findViewById(R.id.activity_send_message_image);
            Picasso.with(this)
                   .invalidate(imageUri); // Force Picasso to reload image even if ImageUri not changed
            Picasso.with(this)
                   .load(imageUri)
                   .into(image);
        }

        if (_request == null) {
            _request = new Messages.SendMessageRequest();
            _request.setRecipient((UserDetails) getIntent().getParcelableExtra(EXTRA_CONTACT));
            _request.setImagePath(imageUri);
        }

        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            View optionsFrame = findViewById(R.id.activity_send_message_optionsFrame);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) optionsFrame.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.BELOW, R.id.include_toolbar);
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()); // converts dp to real pixels
            optionsFrame.setLayoutParams(params);
        }

        _messageEditText = (EditText) findViewById(R.id.activity_send_message_message);
        _recipientButton = (Button) findViewById(R.id.activity_send_message_recipient);
        _progressFrame = findViewById(R.id.activity_send_message_progressFrame);

        _progressFrame.setVisibility(View.GONE);

        _recipientButton.setOnClickListener(this);
        updateButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_REQUEST, _request);
    }

    @Override
    public void onClick(View view) {
        if (view == _recipientButton) {
            selectRecipient();
        }
    }

    private void updateButtons() {
        UserDetails recipient = _request.getRecipient();
        if (recipient != null) {
            _recipientButton.setText("To: " + recipient.getDisplayName());
        } else {
            _recipientButton.setText("Choose Recipient");
        }
    }

    private void selectRecipient() {
        startActivityForResult(new Intent(this, SelectContactActivity.class), REQUEST_SELECT_RECIPIENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_RECIPIENT && resultCode == RESULT_OK) {
            UserDetails selectedContact = data.getParcelableExtra(SelectContactActivity.RESULT_CONTACT);
            _request.setRecipient(selectedContact);
            updateButtons();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_send_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.activity_send_message_menuSend) {
            sendMessage();
            return true;
        }

        return false;
    }

    private void sendMessage() {
        String message = _messageEditText.getText().toString();
        if (message.length() < 2) {
            _messageEditText.setError("Please enter a longer message");
            return;
        }
        _messageEditText.setError(null);

        if(_request.getRecipient() == null) {
            Toast.makeText(this, "Please select a recipient", Toast.LENGTH_SHORT).show();
            selectRecipient();
            return;
        }

        _progressFrame.setVisibility(View.VISIBLE);
        _progressFrame.setAlpha(0);
        _progressFrame.animate()
                .alpha(1)
                .setDuration(250)
                .start();

        _request.setMessage(message);
        bus.post(_request);
    }

    @Subscribe
    public void onMessageSent(Messages.SendMessageResponse response) {
        if (!response.didSucceed()) {
            response.showErrorToast(this);
            _messageEditText.setError(response.getPropertyError("message"));
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
            return;
        }

        Intent data = new Intent();
        data.putExtra(RESULT_MESSAGE, response.Message);
        setResult(RESULT_OK, data);
        finish();
    }
}
