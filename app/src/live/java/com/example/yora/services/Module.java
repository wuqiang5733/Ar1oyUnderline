package com.example.yora.services;

import com.example.yora.infrastructure.Auth;
import com.example.yora.infrastructure.YoraApplication;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;


public class Module {
    public static void register(YoraApplication application) {
        YoraWebService api = createWebService(application);

        new LiveAccountService(application, api);
        new LiveContactService(application, api);
        new LiveMessageService(application, api);
    }

    private static YoraWebService createWebService(YoraApplication application) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(Calendar.class, new DateDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthInterceptor(application.getAuth()));

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(YoraApplication.API_ENDPOINT.toString())
                .setConverter(new GsonConverter(gson))
                .setClient(new OkClient(client))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("X-Student", YoraApplication.STUDENT_TOKEN);
                    }
                })
                .build();

        return adapter.create(YoraWebService.class);
    }

    private static class AuthInterceptor implements Interceptor {
        private Auth _auth;

        public AuthInterceptor(Auth auth) {
            _auth = auth;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (_auth.hasAuthToken()) {
                request = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + _auth.getAuthToken())
                        .build();
            }

            Response response = chain.proceed(request);
            if (response.code() == 401 && _auth.hasAuthToken()) {
                _auth.setAuthToken(null);
            }
            return response;
        }
    }

    private static final String[] DATE_FORMATS = new String[] {
            "yyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyy-MM-dd'T'HH:mm:ss.SSS",
            "yyy-MM-dd'T'HH:mmZ",
            "yyy-MM-dd'T'HH:mm",
            "yyy-MM-dd",
    };

    private static class DateDeserializer implements JsonDeserializer<Calendar> {

        @Override
        public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            for (String format: DATE_FORMATS) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
                    dateFormat.setTimeZone(TimeZone.getDefault());
                    Date date = dateFormat.parse(json.getAsString());
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(date.getTime() + TimeZone.getDefault().getOffset(0));
                    return calendar;
                } catch (ParseException ignored) { } // Move to next format
            }

            throw new JsonParseException("Can not parse date '" + json.getAsString() + "'");
        }
    }
}