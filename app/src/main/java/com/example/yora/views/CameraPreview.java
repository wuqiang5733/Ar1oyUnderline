package com.example.yora.views;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.yora.activities.BaseActivity;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder _surfaceHolder;
    private Camera _camera;
    private Camera.CameraInfo _cameraInfo;
    private boolean _isSurfaceCreaated;

    public CameraPreview(BaseActivity activity) {
        super(activity);
        _isSurfaceCreaated = false;
        _surfaceHolder = getHolder();
        _surfaceHolder.addCallback(this);
    }

    public void setCamera(Camera camera, Camera.CameraInfo cameraInfo) {
        if (_camera != null) {
            try {
                _camera.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, "Could not stop camera preview", e);
            }
        }

        _camera = camera;
        _cameraInfo = cameraInfo;

        if (!_isSurfaceCreaated || camera == null)
            return;

        try {
            _camera.setPreviewDisplay(_surfaceHolder);
            configureCamera();
            _camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Could not start camera preview", e);
        }
    }

    private void configureCamera() {
        Camera.Parameters parameters = _camera.getParameters();

        Camera.Size targetPreviewSize = getClosestSize(getWidth(), getHeight(), parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(targetPreviewSize.width, targetPreviewSize.height);

        Camera.Size targetImageSize = getClosestSize(1024, 1280, parameters.getSupportedPictureSizes());
        parameters.setPictureSize(targetImageSize.width, targetImageSize.height);

        _camera.setDisplayOrientation(90);

        _camera.setParameters(parameters);
    }

    private Camera.Size getClosestSize(int width, int height, List<Camera.Size> supportedSizes) {
        final double ASPECT_TOLERANCE = .1;
        double targetRatio = (double) height/ width;

        Camera.Size targetSize = null;
        double minDifference = Double.MAX_VALUE;

        for (Camera.Size size: supportedSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            int heightDifference = Math.abs(size.height - height);
            if (heightDifference < minDifference) {
                targetSize = size;
                minDifference = heightDifference;
            }
        }

        // If none of supportedSizes is within ASPECT_TOLERANCE, drop the aspect constraint
        if (targetSize == null) {
            minDifference = Double.MAX_VALUE;
            for (Camera.Size size: supportedSizes) {
                int heightDifference = Math.abs(size.height - height);
                if (heightDifference < minDifference) {
                    targetSize = size;
                    minDifference = heightDifference;
                }
            }
        }

        return targetSize;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (_surfaceHolder != holder) {
            _surfaceHolder = holder;
            _surfaceHolder.addCallback(this);
        }

        _isSurfaceCreaated = true;

        if (_camera != null)
            setCamera(_camera, _cameraInfo);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        _isSurfaceCreaated = false;
        _surfaceHolder.removeCallback(this);
        _surfaceHolder = null;

        if (_camera == null)
            return;

        try {
            _camera.stopPreview();
            _camera = null;
            _cameraInfo = null;
        } catch (Exception e) {
            Log.e(TAG, "Could not stop camera preview", e);
        }
    }

    @Override
    protected void onMeasure(int width, int height) {
        // Fix some stretching issues
        width = resolveSize(getSuggestedMinimumWidth(), width);
        height = resolveSize(getSuggestedMinimumHeight(), height);
        setMeasuredDimension(width, height);
    }
}
