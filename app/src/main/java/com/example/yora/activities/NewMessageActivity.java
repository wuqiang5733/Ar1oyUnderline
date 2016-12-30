package com.example.yora.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.yora.R;
import com.example.yora.services.entities.Message;
import com.example.yora.services.entities.UserDetails;
import com.example.yora.views.CameraPreview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class NewMessageActivity extends BaseAuthenticatedActivity implements View.OnClickListener, Camera.PictureCallback {
    public static final String EXTRA_CONTACT = "EXTRA_CONTACT";

    private static final String TAG = "NewMessageActivity";
    private static final String STATE_CAMERA_INDEX = "STATE_CAMERA_INDEX";
    private static final int REQUEST_SEND_MESSAGE = 1;

    private Camera _camera;
    private Camera.CameraInfo _cameraInfo;
    private int _currentCameraIndex;
    private CameraPreview _cameraPreview;
    private UserDetails _userDetails;

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_message);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            _currentCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX);
        } else {
            _currentCameraIndex = 0;
        }

        _cameraPreview = new CameraPreview(this);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_new_message_frame);
        frameLayout.addView(_cameraPreview, 0);

        findViewById(R.id.activity_new_message_switchCamera).setOnClickListener(this);
        findViewById(R.id.activity_new_message_takePicture).setOnClickListener(this);

        _userDetails = getIntent().getParcelableExtra(EXTRA_CONTACT);
    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        if (itemId == R.id.activity_new_message_takePicture) {
            takePicture();
        } else if (itemId == R.id.activity_new_message_switchCamera) {
            switchCamera();
        }
    }

    private void takePicture() {
        _camera.takePicture(null, null, this);
    }

    private void switchCamera() {
        _currentCameraIndex = _currentCameraIndex + 1 < Camera.getNumberOfCameras() ? _currentCameraIndex + 1 : 0;
        establishCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        establishCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_camera != null) {
            _cameraPreview.setCamera(null, null);
            _camera.release();
            _camera = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CAMERA_INDEX, _currentCameraIndex);
    }

    private void establishCamera() {
        if (_camera != null) {
            _cameraPreview.setCamera(null, null);
            _camera.release();
            _camera = null;
        }

        try {
            _camera = Camera.open(_currentCameraIndex);
        } catch (Exception e) {
            Log.e(TAG, "Could not open camera " + _currentCameraIndex, e);
            Toast.makeText(this, "Error establishing camera!", Toast.LENGTH_LONG).show();
            return;
        }

        _cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(_currentCameraIndex, _cameraInfo);
        _cameraPreview.setCamera(_camera, _cameraInfo);

        if (_cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Toast.makeText(this, "Using back camera", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Using front camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bitmap = processBitmap(data);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);

        File outputFile = new File(getCacheDir(), "temp-image");
        outputFile.delete();
        try {
            FileOutputStream fileOutput = new FileOutputStream(outputFile);
            fileOutput.write(output.toByteArray());
            fileOutput.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not save bitmap", e);
            Toast.makeText(this, "Could not save image to temp directory", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra(SendMessageActivity.EXTRA_IMAGE_PATH, Uri.fromFile(outputFile));
        if (_userDetails != null) {
            intent.putExtra(SendMessageActivity.EXTRA_CONTACT, _userDetails);
        }
        startActivityForResult(intent, REQUEST_SEND_MESSAGE);

        bitmap.recycle();
    }

    private Bitmap processBitmap(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        // Fix the problem of rotated and flipped image

        Matrix matrix = new Matrix();
        // Flip horizontally if using front facing cam
        if (_cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Matrix matrixMirror = new Matrix();
            matrixMirror.setValues(new float[] {
                    -1, 0, 0,
                     0, 1, 0,
                     0, 0, 1
            });
            matrix.postConcat(matrixMirror);
        }
        // Rotate
        matrix.postRotate(90);
        Bitmap processedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (bitmap != processedBitmap) {
            bitmap.recycle();
            bitmap = processedBitmap;
        }

        // Scale image if it's larger than MAX_IMAGE_HEIGHT
        if (bitmap.getHeight() > SendMessageActivity.MAX_IMAGE_HEIGHT) {
            float scale = (float) SendMessageActivity.MAX_IMAGE_HEIGHT / bitmap.getHeight();
            int finalWidth = (int) (bitmap.getWidth() * scale);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, SendMessageActivity.MAX_IMAGE_HEIGHT, false);
            if (resizedBitmap != bitmap) {
                bitmap.recycle();
                bitmap = resizedBitmap;
            }
        }

        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SEND_MESSAGE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();

            Message message = data.getParcelableExtra(SendMessageActivity.RESULT_MESSAGE);
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.EXTRA_MESSAGE, message);
            startActivity(intent);
        }
    }
}
