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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseAuthenticatedActivity implements View.OnClickListener {
    private static final int REQUEST_SELECT_IMAGE = 100;// 编辑头像时，选择器（选择照相机与相册）的请求码

    private static final int STATE_VIEWING = 1;
    private static final int STATE_EDITING = 2;

    private static final String BUNDLE_STATE = "BUNDLE_STATE"; // onSaveInstanceState当中要用的 Key
    private static final String BUNDLE_PROGRESS_BAR = "BUNDLE_PROGRESS_BAR";

    private int _currentState;//切换 查看 与 编辑  模式 要用

    private EditText _displayNameText;//显示名  跟 邮件
    private EditText _emailText;

    private ActionMode _editProfileActionMode;

    private ImageView _avatarView;  // 头像跟头像下面的文字
    private View _changeAvatarButton;

    private View _avatarProgressFrame; // 头像上转的那个圈

    private File _tempOutputFile; // 编辑头像时用的临时文件

    private Dialog _progressDialog;  // 更新 Profile 或者 密码的时候，出现的那个提示框

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_profile);
        setNavDrawer(new MainNavDrawer(this));

        if (!isTablet) {// Video37_22min
            View textFields = findViewById(R.id.activity_profile_textFields);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textFields.getLayoutParams();
            params.setMargins(0, params.getMarginStart(), 0, 0);//left,top,right,bottom
            params.setMarginStart(0); //Without it the margin start won't be removed!
            params.removeRule(RelativeLayout.END_OF);
            params.addRule(RelativeLayout.BELOW, R.id.activity_profile_changeAvatar);// 放到头像下面
            textFields.setLayoutParams(params);
        }
        // 点击头像或者头像下面的文字，还有那个有 圆形 ProgressBar 的 FrameLayout 都会生成事件
        _avatarView = (ImageView) findViewById(R.id.activity_profile_avatar);// 头像跟头像下面的文字
        _changeAvatarButton = findViewById(R.id.activity_profile_changeAvatar);

        _avatarProgressFrame = findViewById(R.id.activity_profile_avatarProgressFrame); // 头像上转的那个圈
        _displayNameText = (EditText) findViewById(R.id.activity_profile_displayName);//显示名跟邮件
        _emailText = (EditText) findViewById(R.id.activity_profile_email);
        _tempOutputFile = new File(getExternalCacheDir(), "temp_image.jpg");

        _avatarView.setOnClickListener(this);
        _changeAvatarButton.setOnClickListener(this);
        // 没有下面这一句，是不能点击的，它会不停的转
        _avatarProgressFrame.setVisibility(View.GONE);

        User user = application.getAuth().getUser();
        // 从 User 类当中提取文字，然后在当前Activity上的ToolBar上显示
        // 在 MainNavDrawer 上面也有显示
        // 这个文字可以在 LoginFragment 当中的 onClick 当中设置
        getSupportActionBar().setTitle(user.getDisplayName());

        Picasso.with(this)
                .load(user.getAvatarUrl())
                .into(_avatarView);

        if (savedInstanceState == null) {
            // 也就是刚刚进入这个界面的时候要用这种
            _displayNameText.setText(user.getDisplayName()); //在_displayNameText跟 邮箱名 上也显示用户名
            _emailText.setText(user.getEmail());
            changeState(STATE_VIEWING);
        } else//旋转屏幕的时候，这个保存状态的方式很奇怪，但是很有效
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
    public void UserDetailsUpdated(Account.UserDetailsUpdatedEvent event) {
        Log.e("ProfileActivity", "L111_UserDetailsUpdated_改变ActionBar上的显示，还有头像的变化");
        getSupportActionBar().setTitle(event.User.getDisplayName());
        Picasso.with(this)
                .load(event.User.getAvatarUrl())
                .into(_avatarView);
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
        // 如果是点击了头像或者是头像下面的文字
        if (viewId == R.id.activity_profile_avatar || viewId == R.id.activity_profile_changeAvatar) {
            changeAvatar();
        }
    }

    private void changeAvatar() {
        // Create a list of explicit intents to start activities which can perform ACTION_IMAGE_CAPTURE
        // ResolveInfo : Information that is returned from resolving an intent against an IntentFilter.
        // This partially corresponds to information collected from the AndroidManifest.xml's <intent> tags.
        /*
        *  这一部分是implicit的请求可以拍照的 Intent
        */
        // 申明一个在本系统上可以拍照的Intent的List
        List<Intent> otherImageCaptureIntents = new ArrayList<>();
        //请求系统上所有可以完成 MediaStore.ACTION_IMAGE_CAPTURE 的 Activity
        List<ResolveInfo> otherImageCaptureActivities = getPackageManager()
                .queryIntentActivities(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 0);
        for (ResolveInfo info : otherImageCaptureActivities) {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            // 往那里存放 Image
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(_tempOutputFile));
            otherImageCaptureIntents.add(captureIntent);
        }
       /*
        *  这一部分是implicit的请求可以 选取图片的 Intent
        */
        // Create a chooser and fill it with activities which can perform ACTION_PICK on images
        Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
        selectImageIntent.setType("image/*");
        Intent chooser = Intent.createChooser(selectImageIntent, getString(R.string.ChooserAvatar).toString());

        // Add ACTION_IMAGE_CAPTURE activities to the chooser list
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, otherImageCaptureIntents.toArray(new Parcelable[otherImageCaptureIntents.size()]));

        // Start chooser
        startActivityForResult(chooser, REQUEST_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 这一段代码必须有读取外部存储的权限
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
// 注意：要使用这个Crop 工具，得在manifest当中有这样的申明 ：
// <activity android:name="com.soundcloud.android.crop.CropImageActivity" />

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
    public void onAvatarUpdated(Account.ChangeAvatarResponse response) {
        _avatarProgressFrame.setVisibility(View.GONE);
        if (!response.didSucceed())
            response.showErrorToast(this);
        _avatarView.setImageResource(0); // Force ImageView to refresh image despite its Uri not changed
        _avatarView.setImageURI(Uri.fromFile(_tempOutputFile));
    }

    @Subscribe
    public void onProfileUpdated(Account.UpdateProfileResponse response) {
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
        // 创建笔形的那个编辑菜单
        getMenuInflater().inflate(R.menu.activity_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //点击 编辑  图标 的 CallBack
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
//进入有箭头跟勾号的编辑界面
            _editProfileActionMode = toolbar.startActionMode(new EditProfileActionCallback());
        } else
            throw new IllegalArgumentException("Invalid state: " + state);

    }

    private class EditProfileActionCallback implements ActionMode.Callback {
      //在 style 当中要加入：  <item name="windowActionModeOverlay">true</item>


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
