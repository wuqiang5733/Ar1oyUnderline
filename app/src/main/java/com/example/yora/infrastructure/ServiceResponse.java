package com.example.yora.infrastructure;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.TreeMap;

public abstract class ServiceResponse {
    private static final String TAG = "ServiceResponse";

    private String _operationError;
    private HashMap<String, String> _propertyErrors;
    private TreeMap<String, String> _propertyErrorsCaseInsensitive;
    private boolean _isCritical;

    public ServiceResponse () {
        _propertyErrors = new HashMap<>();
    }

    public ServiceResponse(String operationError) {
        _operationError = operationError;
    }

    public ServiceResponse(String operationError, boolean isCritical) {
        _operationError = operationError;
        _isCritical = isCritical;
    }


    public String getOperationError() {
        return _operationError;
    }

    public void setOperationError(String operationError) {
        _operationError = operationError;
    }

    public boolean isCritical() {
        return _isCritical;
    }

    public void setCritical(boolean critical) {
        _isCritical = critical;
    }

    public void setCriticalError(String criticalError) {
        _isCritical = true;
        _operationError = criticalError;
    }

    public void setPropertyError(String property, String error) {
        _propertyErrors.put(property, error);
    }

    public String getPropertyError(String property) {
        if (_propertyErrorsCaseInsensitive == null || _propertyErrorsCaseInsensitive.size() != _propertyErrors.size()) {
            _propertyErrorsCaseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            _propertyErrorsCaseInsensitive.putAll(_propertyErrors);
        }
        return _propertyErrorsCaseInsensitive.get(property);
    }

    public boolean didSucceed() {
        return (_operationError == null || _operationError.isEmpty()) &&
               (_propertyErrors.size() == 0);
    }

    public void showErrorToast(Context context) {
        if (context == null || _operationError == null || _operationError.isEmpty())
            return;
        try {
            Toast.makeText(context, _operationError, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Can't create error toast", e);
        }
    }
}
