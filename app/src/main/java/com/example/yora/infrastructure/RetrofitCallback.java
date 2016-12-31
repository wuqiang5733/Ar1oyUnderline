package com.example.yora.infrastructure;

import android.util.Log;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public abstract class RetrofitCallback<T extends ServiceResponse> implements Callback<T> {
    private static final String TAG = "RetrofitCallback";
    // Class<T> represents a class object of specific class type 'T'.
    // http://stackoverflow.com/questions/462297/how-to-use-classt-in-java

    protected final Class<T> resultType; // 任何类？

    public RetrofitCallback(Class<T> resultType) {
        this.resultType = resultType;  // 联系上面是说 resultType 可以接收 ServiceResponse 的任何子类 ？
    }

    protected abstract void onResponse(T t);

    @Override
    public void success(T t, Response response) {
        onResponse(t);
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e(TAG, "Error sending request with " + resultType.getName() + " response", error);

        ServiceResponse errorResult;
        try {
            errorResult = resultType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating result type " + resultType.getName(), e);
        }

        if (error.getKind() == RetrofitError.Kind.NETWORK) {
            errorResult.setCriticalError("Unable to connect to Yora servers!");
            onResponse((T) errorResult);
            return;
        }

        if (error.getSuccessType() == null) {
            errorResult.setCriticalError("Unknown error. Please try again.");
            onResponse((T) errorResult);
            return;
        }

        try {
            if (error.getBody() instanceof ServiceResponse) {
                ServiceResponse result = (ServiceResponse) error.getBody();
                if (result.didSucceed()) {
                    result.setCriticalError("Unknown error. Please try again.");
                }

                onResponse((T) result);
            } else {
                throw new RuntimeException("Result class " + resultType.getName() + " does not extend ServiceResponse");
            }
        } catch (Exception e) {
            Log.e(TAG, "Unknown error", e);
            errorResult.setCriticalError("Unknown error. Please try again.");
            onResponse((T) errorResult);
        }
    }
}
