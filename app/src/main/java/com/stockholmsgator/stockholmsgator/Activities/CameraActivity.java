package com.stockholmsgator.stockholmsgator.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
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
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stockholmsgator.stockholmsgator.Classes.CropBox;
import com.stockholmsgator.stockholmsgator.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;

    private TextureView textureView;
    private TextView detectedText;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraDevice cameraDevice;
    private String cameraID;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimensions;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundHandlerThread;

    // för zoom
    private float fingerSpacing = 0;
    private float zoomLevel = 1;
    private float maximumZoomLevel;
    private Rect zoom;
    private Rect cropRect;

    private CropBox cropBox;
    int counter = 0;
    TextRecognizer textRecognizer;
    private FloatingActionButton pictureBtn;
    public static String currentPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initComponents();
        addListeners();
    }

    private void initComponents() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        cropRect = new Rect(0,(size.y/2) - size.y/10,size.x,(size.y/2) + size.y/10);
        cropBox = new CropBox(this,cropRect);

        ViewGroup layout = findViewById(R.id.maskView);
        layout.addView(cropBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        textRecognizer = new TextRecognizer.Builder(this).build();
        detectedText = findViewById(R.id.detectedText);
        pictureBtn = findViewById(R.id.takePictureBtn);
        textureView = findViewById(R.id.cameraView);
        textureView.setSurfaceTextureListener(textureListener);
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

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
            counter++;
            if (counter >= 15){
                counter = 0;
                detectText();
            }
        }
    };

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());

        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }

                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                Toast.makeText(CameraActivity.this, "Configuration changed", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }



    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
    }



    private void addListeners() {
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("searchText",detectedText.getText());
                setResult(RESULT_OK,data);
                finish();
            }
        });

        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
                    Rect rect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    if (rect == null) return false;
                    float currentFingerSpacing;

                    if (event.getPointerCount() == 2) { //Multi touch.
                        currentFingerSpacing = getFingerSpacing(event);
                        float delta = 0.05f; //Control this value to control the zooming sensibility
                        if (fingerSpacing != 0) {
                            if (currentFingerSpacing > fingerSpacing) { //Don't over zoom-in
                                if ((maximumZoomLevel - zoomLevel) <= delta) {
                                    delta = maximumZoomLevel - zoomLevel;
                                }
                                zoomLevel = zoomLevel + delta;
                            } else if (currentFingerSpacing < fingerSpacing){ //Don't over zoom-out
                                if ((zoomLevel - delta) < 1f) {
                                    delta = zoomLevel - 1f;
                                }
                                zoomLevel = zoomLevel - delta;
                            }
                            float ratio = (float) 1 / zoomLevel; //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect
                            //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
                            int croppedWidth = rect.width() - Math.round((float)rect.width() * ratio);
                            int croppedHeight = rect.height() - Math.round((float)rect.height() * ratio);
                            //Finally, zoom represents the zoomed visible area
                            zoom = new Rect(croppedWidth/2, croppedHeight/2,
                                    rect.width() - croppedWidth/2, rect.height() - croppedHeight/2);
                            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                        }
                        fingerSpacing = currentFingerSpacing;
                    } else { //Single touch point, needs to return true in order to detect one more touch point
                        return true;
                    }
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    return true;
                } catch (CameraAccessException e) {
                    throw new RuntimeException("can not access camera.", e);
                }
            }
        });
    }

    //Determine the space between the first two fingers
    @SuppressWarnings("deprecation")
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void takePicture() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());

        Size[] jpegSizes = null;

        jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

        int width = 640;
        int height = 480;

        if (jpegSizes != null && jpegSizes.length > 0) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        List<Surface> outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(imageReader.getSurface());
        outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());
        if (zoom != null) {
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
        }else{
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        }

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmap2 = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(), bitmap.getHeight(), matrix,true);

                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap2, 0,(bitmap2.getHeight()/2) - bitmap2.getHeight()/10,
                        bitmap2.getWidth(), bitmap2.getHeight()/5);

                if (image != null){
                    image.close();

                }
                String searchText = detectText(croppedBitmap);

                Intent data = new Intent();
                data.putExtra("searchText",searchText);
                setResult(RESULT_OK,data);
                finish();
            }
        };

        imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);

        final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);

            }

        };

        cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(), captureCallback, backgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }

            @Override
            public void onClosed(@NonNull CameraCaptureSession session) {
                super.onClosed(session);
            }
        }, backgroundHandler);
    }

    private void openCamera() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraID = cameraManager.getCameraIdList()[0];

        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
        maximumZoomLevel = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        cameraManager.openCamera(cameraID, stateCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    private void startBackgroundThread() {
        backgroundHandlerThread = new HandlerThread("Camera background");
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        try {
            stopBackgroundThread();
            closeCamera();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private void stopBackgroundThread() throws InterruptedException {
        backgroundHandlerThread.quitSafely();
        backgroundHandlerThread.join();
        backgroundHandlerThread = null;
        backgroundHandler = null;
    }

    private void closeCamera(){
        if (cameraCaptureSession != null){
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private String detectText(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
            return "";
        }

        if (textRecognizer != null) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                sb.append(item.getValue());
            }
            return sb.toString();
        }
        return "";
    }

    private void detectText() {
        Bitmap bitmap = textureView.getBitmap();
        final Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0,(bitmap.getHeight()/2) - bitmap.getHeight()/10,
                bitmap.getWidth(), bitmap.getHeight()/5);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String str = detectText(croppedBitmap).split("\\R")[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (str.isEmpty()){
                            detectedText.setText("Ingen text funnen");
                        }
                        else {
                            detectedText.setText(str);
                        }
                    }
                });
            }
        });
        thread.start();
    }
}
