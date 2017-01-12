package com.example.yora.services;


import android.util.Log;

import com.example.yora.infrastructure.Auth;
import com.example.yora.infrastructure.User;
import com.example.yora.infrastructure.YoraApplication;
import com.squareup.otto.Subscribe;

public class InMemoryAccountService extends BaseInMemoryService {

    public InMemoryAccountService(YoraApplication application) {
        super(application);
    }

    @Subscribe
    public void updateProfile(final Account.UpdateProfileRequest request) {
        final Account.UpdateProfileResponse response = new Account.UpdateProfileResponse();
        if (request.DisplayName.equals("Dariush")) {
            response.setPropertyError("displayName", "You may not be named Dariush!");
        }

        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                User user = application.getAuth().getUser();
                user.setDisplayName(request.DisplayName);
                user.setEmail(request.Email);
// 去往 ProfileActivity 当中的     public void onProfileUpdated(Account.UpdateProfileResponse response) {
                bus.post(response);
                // 去往 MainNavDrawer 当中的 public void UserDetailsUpdated(Account.UserDetailsUpdatedEvent event)
// 去往 ProfileActivity 当中的               public void UserDetailsUpdated(Account.UserDetailsUpdatedEvent event)
                bus.post(new Account.UserDetailsUpdatedEvent(user));
            }
        }, 2000, 3000);
    }

    @Subscribe
    public void updateAvatar(final Account.ChangeAvatarRequest request) {
        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                User user = application.getAuth().getUser();
                user.setAvatarUrl(request.NewAvatarUri.toString() /*Should set to cloud image url*/);

                bus.post(new Account.ChangeAvatarResponse());
                bus.post(new Account.UserDetailsUpdatedEvent(user));
            }
        }, 4000, 5000);
    }

    @Subscribe
    public void changePassword(Account.ChangePasswordRequest request) {
        Account.ChangePasswordResponse response = new Account.ChangePasswordResponse();

        if (!request.NewPassword.equals(request.ConfirmNewPassword))
            response.setPropertyError("confirmNewPassword", "Passwords must match!");

        if (request.NewPassword.length() < 3)
            response.setPropertyError("newPassword", "Passwords must larger than 3 characters!");

        if (response.didSucceed())
            application.getAuth().getUser().setHasPassword(true);

        postDelayed(response);
    }

    @Subscribe
    public void loginWithUserName(final Account.LoginWithUserNameRequest request) {
        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                Account.LoginWithUserNameResponse response = new Account.LoginWithUserNameResponse();

                if (request.UserName.equals("dariush"))
                    response.setPropertyError("userName", "Invalid username or password");

                loginUser(new Account.UserResponse());//就在本 Activity 当中
                bus.post(response);
            }
        }, 1000, 2000);
    }

    @Subscribe
    public void loginWithExternalToken(Account.LoginWithExternalTokenRequest request) {
        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                Account.LoginWithExternalTokenResponse response = new Account.LoginWithExternalTokenResponse();
                loginUser(response);
                bus.post(response);
            }
        }, 1000, 2000);
    }

    @Subscribe
    public void loginWithLocalToken(Account.LoginWithLocalTokenRequest request) {
        Log.e("InMemoryAccountService","L97_@Subscribe_loginWithLocalToken");
        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                Account.LoginWithLocalTokenResponse response = new Account.LoginWithLocalTokenResponse();
                loginUser(response);
                bus.post(response);
                // AuthenticationActivity : L36_@Subscribe_onLoginWithLocalToken
            }
        }, 1000, 2000);
    }

    @Subscribe
    public void register(Account.RegisterRequest request) {
        Log.e("InMemoryAccountService", "L109_@Subscribe_register_注册_调用loginUser");
        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                //去往 RegisterActivity 下的 L56 onRegisterResponse ？
                Account.RegisterResponse response = new Account.RegisterResponse();
                loginUser(response);
                bus.post(response);
            }
        }, 1000, 2000);
    }

    @Subscribe
    public void externalRegister(Account.RegisterWithExternalTokenRequest request) {
        invokeDelayed(new Runnable() {
            @Override
            public void run() {
                Account.RegisterWithExternalTokenResponse response = new Account.RegisterWithExternalTokenResponse();
                loginUser(response);
                bus.post(response);
            }
        }, 1000, 2000);
    }

    @Subscribe
    public void updateGcmRegistration(Account.UpdateGcmRegistrationRequest request) {
        postDelayed(new Account.UpdateGcmRegistrationResponse());
    }

    private void loginUser(Account.UserResponse response) {
        Auth auth = application.getAuth();
        User user = auth.getUser();
        Log.e("InMemoryAccountService", "L140_loginUser_注册_从Auth当中生成一个User，设置他的Name,Email,Avatar,ID，setAuthToken，并且将其设为已经注册，调用UserDetailsUpdatedEvent(user)");
        user.setDisplayName("Dariush Lotfi");
        user.setUserName("dlotif");
        user.setEmail("me@dlotfi.ir");
        user.setAvatarUrl("http://www.gravatar.com/avatar/1?d=identicon");
        user.setLoggedIn(true);
        user.setId(123);
        bus.post(new Account.UserDetailsUpdatedEvent(user));

        auth.setAuthToken("fakeauthtoken");

        response.DisplayName = user.getDisplayName();
        response.UserName = user.getUserName();
        response.Email = user.getEmail();
        response.AvatarUrl = user.getAvatarUrl();
        response.Id = user.getId();
        response.AuthToken = auth.getAuthToken();
    }
}
