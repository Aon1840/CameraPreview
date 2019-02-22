package com.example.customcamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.support.constraint.solver.widgets.Helper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        context = getContext();
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
            int chosenSize = getPictureSizeIndexForHeight(sizeList, 800);
            parameters.setPictureSize(sizeList.get(chosenSize).width, sizeList.get(chosenSize).height);

            parameters.set("orientation", "portrait");
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPictureSizeIndexForHeight(List<Camera.Size> sizeList, int height) {
        int chosenHeight = -1;
        for(int i=0; i<sizeList.size(); i++) {
            if(sizeList.get(i).height < height) {
                chosenHeight = i-1;
                if(chosenHeight==-1)
                    chosenHeight = 0;
                break;
            }
        }
        return chosenHeight;
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
