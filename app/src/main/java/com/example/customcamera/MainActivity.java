package com.example.customcamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private Camera mCamera;
    private HorizontalScrollView horizontalScrollView;
    private final int PERMISSION_CALLBACK_CONSTANT = 1000;
    private ImageView ivCapture, ivFilter;
    private FrameLayout rlCameraPreview;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();

        checkAndGivePermission();
    }

    private void checkAndGivePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_CALLBACK_CONSTANT);
        } else {
            initial();
        }
    }

    private void initial() {
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        if (rlCameraPreview != null) {
            rlCameraPreview.addView(mPreview);
        }
    }

    private void bindView() {
        ivCapture = (ImageView) findViewById(R.id.ivCapture);
        ivFilter = (ImageView) findViewById(R.id.ivFilter);
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.hscFilterLayout);
        rlCameraPreview = (FrameLayout) findViewById(R.id.rlCameraPreview);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }
}
