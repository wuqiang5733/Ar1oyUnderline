package com.example.yora.services;

import com.example.yora.infrastructure.Auth;
import com.example.yora.infrastructure.RetrofitCallback;
import com.example.yora.infrastructure.RetrofitCallbackPost;
import com.example.yora.infrastructure.User;
import com.example.yora.infrastructure.YoraApplication;
import com.squareup.otto.Subscribe;

import java.io.File;

import retrofit.mime.TypedFile;


public class LiveAccountService extends BaseLiveService {
    private final Auth _auth;

    protected LiveAccountService(YoraApplication application, YoraWebService api) {
        super(application, api);
        _auth = application.getAuth();
    }

    @Subscribe
    public void register(Account.RegisterRequest request) {
        api.register(request, new RetrofitCallback<Account.RegisterResponse>(Account.RegisterResponse.class) {
               @Override
               protected void onResponse(Account.RegisterResponse registerResponse) {
                   if (registerResponse.didSucceed()) {
                       loginUser(registerResponse);
                   }
                   bus.post(registerResponse);
               }
           });
    }

    @Subscribe
    public void loginWithUsername(Account.LoginWithUserNameRequest request) {
        api.login(request.UserName, request.Password, "android", "password",
            new RetrofitCallback<YoraWebService.LoginResponse>(YoraWebService.LoginResponse.class) {
               @Override
               protected void onResponse(final YoraWebService.LoginResponse loginResponse) {
                   if (!loginResponse.didSucceed()) {
                       Account.LoginWithUserNameResponse response = new Account.LoginWithUserNameResponse();
                       response.setPropertyError("userName", loginResponse.ErrorDescription);
                       bus.post(response);
                       return;
                   }

                   _auth.setAuthToken(loginResponse.Token);
                   api.getAccount(new RetrofitCallback<Account.LoginWithLocalTokenResponse>(Account.LoginWithLocalTokenResponse.class) {
                          @Override
                          protected void onResponse(Account.LoginWithLocalTokenResponse loginWithLocalTokenResponse) {
                              if (!loginWithLocalTokenResponse.didSucceed()) {
                                  Account.LoginWithUserNameResponse response = new Account.LoginWithUserNameResponse();
                                  response.setOperationError(loginWithLocalTokenResponse.getOperationError());
                                  bus.post(response);
                                  return;
                              }

                              loginUser(loginWithLocalTokenResponse);
                              bus.post(new Account.LoginWithUserNameResponse());
                          }
                      });
               }
           });
    }

    @Subscribe
    public void loginWithLocalToken(Account.LoginWithLocalTokenRequest request) {
        api.getAccount(new RetrofitCallbackPost<Account.LoginWithLocalTokenResponse>(Account.LoginWithLocalTokenResponse.class, bus) {
               @Override
               protected void onResponse(Account.LoginWithLocalTokenResponse loginWithLocalTokenResponse) {
                   loginUser(loginWithLocalTokenResponse);
                   super.onResponse(loginWithLocalTokenResponse);
               }
           });
    }

    @Subscribe
    public void updateProfile(Account.UpdateProfileRequest request) {
        api.updateProfile(request, new RetrofitCallbackPost<Account.UpdateProfileResponse>(Account.UpdateProfileResponse.class, bus) {
               @Override
               protected void onResponse(Account.UpdateProfileResponse response) {
                   User user = _auth.getUser();
                   user.setDisplayName(response.DisplayName);
                   user.setEmail(response.Email);
                   super.onResponse(response);
                   bus.post(new Account.UserDetailsUpdatedEvent(user));
               }
           });
    }

    @Subscribe
    public void updateAvatar(Account.ChangeAvatarRequest request) {
        api.updateAvatar(
                new TypedFile("image/jpeg", new File(request.NewAvatarUri.getPath())),
                new RetrofitCallbackPost<Account.ChangeAvatarResponse>(Account.ChangeAvatarResponse.class, bus) {
                    @Override
                    protected void onResponse(Account.ChangeAvatarResponse response) {
                        User user = _auth.getUser();
                        user.setAvatarUrl(response.AvatarUrl);
                        super.onResponse(response);
                        bus.post(new Account.UserDetailsUpdatedEvent(user));
                    }
                });
    }

    @Subscribe
    public void changePassword(Account.ChangePasswordRequest request) {
        api.updatePassword(request, new RetrofitCallbackPost<Account.ChangePasswordResponse>(Account.ChangePasswordResponse.class, bus) {
               @Override
               protected void onResponse(Account.ChangePasswordResponse response) {
                   if (response.didSucceed()) {
                       _auth.getUser().setHasPassword(true);
                   }
                   super.onResponse(response);
               }
           });
    }

    @Subscribe
    public void loginWithExternalToken(Account.LoginWithExternalTokenRequest request) {
        api.loginWithExternalToken(request, new RetrofitCallbackPost<Account.LoginWithExternalTokenResponse>(Account.LoginWithExternalTokenResponse.class, bus) {
               @Override
               protected void onResponse(Account.LoginWithExternalTokenResponse response) {
                   loginUser(response);
                   super.onResponse(response);
               }
           });
    }

    @Subscribe
    public void registerWithExternalToken(Account.RegisterWithExternalTokenRequest request) {
        api.registerExternal(request, new RetrofitCallbackPost<Account.RegisterWithExternalTokenResponse>(Account.RegisterWithExternalTokenResponse.class, bus) {
               @Override
               protected void onResponse(Account.RegisterWithExternalTokenResponse response) {
                   loginUser(response);
                   super.onResponse(response);
               }
           });
    }


    @Subscribe void registerGcm(Account.UpdateGcmRegistrationRequest request) {
        api.updateGcmRegistration(request, new RetrofitCallbackPost<>(Account.UpdateGcmRegistrationResponse.class, bus));
    }

    private void loginUser(Account.UserResponse response) {
        if (response.AuthToken != null && !response.AuthToken.isEmpty()) {
            _auth.setAuthToken(response.AuthToken);
        }

        User user = _auth.getUser();
        user.setId(response.Id);
        user.setDisplayName(response.DisplayName);
        user.setUserName(response.UserName);
        user.setEmail(response.Email);
        user.setAvatarUrl(response.AvatarUrl);
        user.setHasPassword(response.HasPassword);
        user.setLoggedIn(true);

        bus.post(new Account.UserDetailsUpdatedEvent(user));
    }
}
