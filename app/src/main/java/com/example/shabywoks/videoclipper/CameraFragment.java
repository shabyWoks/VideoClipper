package com.example.shabywoks.videoclipper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraFragment extends Fragment {

    private Camera                      mCamera;
    private CameraPreview               mPreview;
    private FrameLayout                 preview;

    private TextureView                 textureView;
    private Size                        previewSize;
    private CaptureRequest.Builder      captureRequestBuilder;
    private CameraCaptureSession        cameraCaptureSession;

    private CameraDevice                cameraDevice;
    private Surface                     surface;
    private Button                      button;

    private Uri                         uri;
    private Bitmap                      bmp;

    private TextView                    cancel;
    private TextView                    confirm;
    private ImageReader                 imageReader;

    private int                         captureCount = 0;
    private String                      basePath = "/data/data/yourapp/app_data/imageDir/";

    public CameraFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_camera, container, false);

        textureView     = (TextureView) v.findViewById(R.id.texture);
        button          = (Button)      v.findViewById(R.id.clickCapture);
        cancel          = (TextView)    v.findViewById(R.id.capture_cancel);
        confirm         = (TextView)    v.findViewById(R.id.capture_confirm);

        textureView.setSurfaceTextureListener(textureViewListener);
        button.setOnClickListener(clickListener);
        cancel.setOnClickListener(cancelClickListener);
        confirm.setOnClickListener(confirmClickListener);

        return v;
    }

    private final Button.OnClickListener clickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            captureImage();
        }
    } ;

    public void enableAfterCaptureControl(final boolean enable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    cancel.setVisibility(View.VISIBLE);
                    confirm.setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);
                } else {
                    cancel.setVisibility(View.INVISIBLE);
                    confirm.setVisibility(View.INVISIBLE);
                    button.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File createdFile = new File(uri.toString());
            if(createdFile.exists()) {
                createdFile.delete();
            }

            uri = null;
            createCameraCapturePreview();
            enableAfterCaptureControl(false);
        }
    };

    View.OnClickListener confirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread() {
                @Override
                public void run() {
                    while(uri == null) {
                        try {
                            sleep(100);
                        }
                        catch (Exception e) {

                        }
                    }
                    captureCount ++;

                    ImageHolder.addImage(bmp, uri.toString());
                    enableAfterCaptureControl(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createCameraCapturePreview();
                            ((ImageSelector)getActivity()).update();
                        }
                    });

                    uri = null;
                }
            }.start();

        }
    };

    private final TextureView.SurfaceTextureListener textureViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    public void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager cm = (CameraManager)this.getActivity().getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cm.getCameraIdList()[0];
                CameraCharacteristics cc = cm.getCameraCharacteristics(cameraId);
                StreamConfigurationMap streamConfigs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert streamConfigs != null;
                previewSize = streamConfigs.getOutputSizes(SurfaceTexture.class)[0];

                cm.openCamera(cameraId, stateCallback, null);

            }
            catch (SecurityException ex) {
            }
            catch (Exception ex) {
            }

        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraCapturePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            System.out.println("Camera Error");
        }
    };

    private void createCameraCapturePreview() {
        try {
            SurfaceTexture st = textureView.getSurfaceTexture();
            assert st!= null;
            st.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            surface = new Surface(st);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), captureSessionCallback, null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final CameraCaptureSession.StateCallback captureSessionCallback =  new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                cameraCaptureSession = session;
                updatePreview();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
        }
    };

    protected void updatePreview() {
        if(null == cameraDevice) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureImage() {
        if (cameraDevice == null) return;

        try {
            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);

            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(imageReader.getSurface());
//            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            imageReader.setOnImageAvailableListener(imageAvailableListener, null);
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(final ImageReader reader) {

            new Thread() {
                @Override
                public void run() {
                    Image img                   = reader.acquireLatestImage();
                    final Image.Plane[] planes  = img.getPlanes();
                    final ByteBuffer buffer     = planes[0].getBuffer();
                    byte[] byteArray            = new byte[buffer.remaining()];
                    buffer.get(byteArray);
                    bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    uri = getImageUri(getContext(), bmp);
                }
            }.start();

            enableAfterCaptureControl(true);
        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "CAPTURE" + captureCount, null);
        return Uri.parse(path);
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureViewListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.closeCamera();
    }
}






