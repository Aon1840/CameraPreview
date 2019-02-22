package com.example.customcamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Helper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceView surfaceViewCamera;
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();

                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size bestPreviewSize = getBestPreviewSize(parameters.getSupportedPreviewSizes(), surfaceViewCamera.getWidth(), surfaceViewCamera.getHeight());
                parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
                Camera.Size bestPictureSize = getBestPictureSize(parameters.getSupportedPictureSizes());
                parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


                parameters.set("orientation","portrait");
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public static Camera.Size getBestPictureSize(@NonNull List<Camera.Size> pictureSizeList) {
        Camera.Size bestPictureSize = null;
        for (Camera.Size pictureSize : pictureSizeList) {
            if (bestPictureSize == null ||
                    (pictureSize.height >= bestPictureSize.height &&
                            pictureSize.width >= bestPictureSize.width)) {
                bestPictureSize = pictureSize;
            }
        }
        return bestPictureSize;
    }

    public static Camera.Size getBestPreviewSize(@NonNull List<Camera.Size> previewSizeList, int previewWidth, int previewHeight) {
        Camera.Size bestPreviewSize = null;
        for (Camera.Size previewSize : previewSizeList) {
            if (bestPreviewSize != null) {
                int diffBestPreviewWidth = Math.abs(bestPreviewSize.width - previewWidth);
                int diffPreviewWidth = Math.abs(previewSize.width - previewWidth);
                int diffBestPreviewHeight = Math.abs(bestPreviewSize.height - previewHeight);
                int diffPreviewHeight = Math.abs(previewSize.height - previewHeight);
                if (diffPreviewWidth + diffPreviewHeight < diffBestPreviewWidth + diffBestPreviewHeight) {
                    bestPreviewSize = previewSize;
                }
            } else {
                bestPreviewSize = previewSize;
            }
        }
        return bestPreviewSize;
    }

    public static int getCameraDisplayOrientation(Activity activity, int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }
        int orientation = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            orientation = (cameraInfo.orientation + degree) % 360;
            orientation = (360 - orientation) % 360;
        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            orientation = (cameraInfo.orientation - degree + 360) % 360;
        }
        return orientation;
    }

    public static void setImageOrientation(File file, int orientation) {
        if (file != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(file.getPath());
                String orientationValue = String.valueOf(getOrientationExifValue(orientation));
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientationValue);
                exifInterface.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getOrientationExifValue(int orientation) {
        switch (orientation) {
            case 90:
                return ExifInterface.ORIENTATION_ROTATE_90;
            case 180:
                return ExifInterface.ORIENTATION_ROTATE_180;
            case 270:
                return ExifInterface.ORIENTATION_ROTATE_270;
            default:
                return ExifInterface.ORIENTATION_NORMAL;
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
//        if (mHolder.getSurface() == null) {
//            return;
//        }
//
//        try {
//            mCamera.stopPreview();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//
//        } catch (Exception e) {
//            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//        }
        refreshCamera(mCamera);
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
    }
}
