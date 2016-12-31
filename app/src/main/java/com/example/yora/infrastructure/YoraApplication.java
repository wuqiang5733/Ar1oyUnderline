package com.example.yora.infrastructure;

import android.app.Application;
import android.net.Uri;

import com.example.yora.services.Module;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class YoraApplication extends Application {
    public static final Uri API_ENDPOINT = Uri.parse("http://yora-playground.3dbuzz.com");
    public static final String STUDENT_TOKEN = "ebce01083a6340a0ba557747dc233107";

    private Auth _auth;
    private Bus _bus;
    private Picasso _authedPicasso;

    public YoraApplication() {
        // It's also correct; but because object has not been completely initiallized at this point,
        // it may cause problem in Auth.
        // _auth = new Auth(this);

        _bus = new Bus();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _auth = new Auth(this);
        createAuthedPicasso();
        Module.register(this);
    }

    private void createAuthedPicasso() {
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + getAuth().getAuthToken())
                        .addHeader("X-Student", STUDENT_TOKEN)
                        .build();
                return chain.proceed(newRequest);
            }
        });

        _authedPicasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(client))
                .build();
    }

    public Picasso getAuthedPicasso() {
        return _authedPicasso;
    }

    public Auth getAuth() {
        return _auth;
    }

    public Bus getBus() {
        return _bus;
    }
}
