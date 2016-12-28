package com.example.yora.activities;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.example.yora.R;
import com.example.yora.dialogs.ChangePasswordDialog;
import com.example.yora.infrastructure.User;
import com.example.yora.services.Account;
import com.example.yora.views.MainNavDrawer;
import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseAuthenticatedActivity implements View.OnClickListener {
    private static final int REQUEST_SELECT_IMAGE = 100;

    private static final int STATE_VIEWING = 1;
    private static final int STATE_EDITING = 2;

    private static final String BUNDLE_STATE = "BUNDLE_STATE";
    private static final String BUNDLE_PROGRESS_BAR = "BUNDLE_PROGRESS_BAR";

    private int _currentState;
    private EditText _displayNameText;
    private EditText _emailText;
    private View _changeAvatarButton;
    private ActionMode _editProfileActionMode;
    private ImageView _avatarView;
    private View _avatarProgressFrame;
    private File _tempOutputFile;
    private Dialog _progressDialog;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_profile);
        setNavDrawer(new MainNavDrawer(this));

        if (!isTablet) {
            View textFields = findViewById(R.id.activity_profile_textFields);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textFields.getLayoutParams();
            params.setMargins(0, params.getMarginStart(), 0, 0);
            params.setMarginStart(0); //Without it the margin start won't be removed!
            params.removeRule(RelativeLayout.END_OF);
            params.addRule(RelativeLayout.BELOW, R.id.activity_profile_changeAvatar);
            textFields.setLayoutParams(params);
        }

        _avatarView = (ImageView) findViewById(R.id.activity_profile_avatar);
        _avatarProgressFrame = findViewById(R.id.activity_profile_avatarProgressFrame);
        _changeAvatarButton = findViewById(R.id.activity_profile_changeAvatar);
        _displayNameText = (EditText) findViewById(R.id.activity_profile_displayName);
        _emailText = (EditText) findViewById(R.id.activity_profile_email);
        _tempOutputFile = new File(getExternalCacheDir(), "temp_image.jpg");

        _avatarView.setOnClickListener(this);
        _changeAvatarButton.setOnClickListener(this);
        _avatarProgressFrame.setVisibility(View.GONE);

        User user = application.getAuth().getUser();
        getSupportActionBar().setTitle(user.getDisplayName());

        if (savedInstanceState == null) {
            _displayNameText.setText(user.getDisplayName());
            _emailText.setText(user.getEmail());
            changeState(STATE_VIEWING);
        }
        else
            changeState(savedInstanceState.getInt(BUNDLE_STATE));

        if (savedInstanceState != null)
            setProgressBarVisible(savedInstanceState.getBoolean(BUNDLE_PROGRESS_BAR));
    }

    private void setProgressBarVisible(boolean visible) {
        if (visible) {
            _progressDialog = new ProgressDialog.Builder(this)
                    .setTitle("Updating Profile")
                    .setCancelable(false)
                    .show();
        } else if (_progressDialog != null) {
            _progressDialog.dismiss();
            _progressDialog = null;
        }
    }

    @Subscribe
    public void UserDetailsUpdated(Account.UserDetailsUpdatedEvent event){
        getSupportActionBar().setTitle(event.User.getDisplayName());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_STATE, _currentState);
        outState.putBoolean(BUNDLE_PROGRESS_BAR, _progressDialog != null);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.activity_profile_avatar || viewId == R.id.activity_profile_changeAvatar) {
            changeAvatar();
        }
    }

    private void changeAvatar() {
        // Create a list of explicit intents to start activities which can perform ACTION_IMAGE_CAPTURE
        List<Intent> otherImageCaptureIntents = new ArrayList<>();
        List<ResolveInfo> otherImageCaptureActivities = getPackageManager()
                .queryIntentActivities(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 0);
        for (ResolveInfo info : otherImageCaptureActivities) {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(_tempOutputFile));
            otherImageCaptureIntents.add(captureIntent);
        }

        // Create a chooser and fill it with activities which can perform ACTION_PICK on images
        Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
        selectImageIntent.setType("image/*");
        Intent chooser = Intent.createChooser(selectImageIntent, "Chooser Avatar");

        // Add ACTION_IMAGE_CAPTURE activities to the chooser list
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, otherImageCaptureIntents.toArray(new Parcelable[otherImageCaptureIntents.size()]));

        // Start chooser
        startActivityForResult(chooser, REQUEST_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            _tempOutputFile.delete();
            return;
        }

        Uri tempFileUri = Uri.fromFile(_tempOutputFile);
        if (requestCode == REQUEST_SELECT_IMAGE) {
            Uri outputFileUri;

            if (data != null && (data.getAction() == null || !data.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE)))
                outputFileUri = data.getData(); // If the user selected an image
            else
                outputFileUri = tempFileUri; // User took a picture

            new Crop(outputFileUri)
            .asSquare()
            .output(tempFileUri)
            
            .start(this);
        } else if (requestCode == Crop.REQUEST_CROP) {
            _avatarProgressFrame.setVisibility(View.VISIBLE);
            bus.post(new Account.ChangeAvatarRequest(tempFileUri));
        }
    }

    @Subscribe
    public void onAvatarUpdated(Account.ChangeAvatarResponse response){
        _avatarProgressFrame.setVisibility(View.GONE);
        if (!response.didSucceed())
            response.showErrorToast(this);
        _avatarView.setImageResource(0); // Force ImageView to refresh image despite its Uri not changed
        _avatarView.setImageURI(Uri.fromFile(_tempOutputFile));
    }

    @Subscribe public void onProfileUpdated(Account.UpdateProfileResponse response) {
        setProgressBarVisible(false);
        if (!response.didSucceed()) {
            response.showErrorToast(this);
            changeState(STATE_EDITING);
        }
        _displayNameText.setError(response.getPropertyError("displayName"));
        _emailText.setError(response.getPropertyError("email"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.activity_profile_menuEdit) {
            changeState(STATE_EDITING);
            return true;
        } else if (itemId == R.id.activity_profile_menuChangePassword) {
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null); // When we hit back key the transaction will be undone
            ChangePasswordDialog dialog = new ChangePasswordDialog();
            dialog.show(transaction, null);
            return true;
        }

        return false;
    }

    private void changeState(int state) {
        if (state == _currentState)
            return;

        _currentState = state;
        if (state == STATE_VIEWING) {
            _displayNameText.setEnabled(false);
            _emailText.setEnabled(false);
            _changeAvatarButton.setVisibility(View.VISIBLE);

            if (_editProfileActionMode != null) {
                _editProfileActionMode.finish();
                _editProfileActionMode = null;
            }
        } else if (state == STATE_EDITING) {
            _displayNameText.setEnabled(true);
            _emailText.setEnabled(true);
            _changeAvatarButton.setVisibility(View.GONE);

            _editProfileActionMode = toolbar.startActionMode(new EditProfileActionCallback());
        } else
            throw new IllegalArgumentException("Invalid state: " + state);

    }

    private class EditProfileActionCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.activity_profile_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.activity_profile_edit_menuDone) {
                setProgressBarVisible(true);
                changeState(STATE_VIEWING);
                bus.post(new Account.UpdateProfileRequest(
                        _displayNameText.getText().toString(),
                        _emailText.getText().toString()));
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (_currentState != STATE_VIEWING) { // When hitting cancel
                User user = application.getAuth().getUser();
                _displayNameText.setText(user.getDisplayName());
                _emailText.setText(user.getEmail());
                changeState(STATE_VIEWING);
            }
        }
    }
}
