package com.mudah.my.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.lib701.utils.ACUtils;
import com.lib701.utils.FileUtils;
import com.lib701.utils.ImageUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.ApiConfigs;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.widgets.CropImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditPictureActivity extends Activity {

    // Static final constants
    public static final int RESULT_FAILED = 3;
    private static final int ROTATE_NINETY_DEGREES = 90;

    Bitmap croppedImage;
    CropImageView cropImageView;
    private String selectedImgPath = Constants.EMPTY_STRING;
    private ProgressBar pbLoading;
    private final View.OnClickListener editImgOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_crop:
                    boolean cropResult = false;
                    String croppedFilePath = Constants.EMPTY_STRING;
                    ACUtils.logCrashlytics("cropping picture");
                    try {
                        pbLoading.setVisibility(View.VISIBLE);
                        croppedImage = cropImageView.getCroppedImage();
                        if (croppedImage != null) {
                            croppedFilePath = createCroppedFile();
                            if (!ACUtils.isEmpty(croppedFilePath)) {
                                cropResult = FileUtils.saveBitmapToFile(croppedImage, croppedFilePath);
                            }
                        }

                        if (cropResult) {
                            Intent data = new Intent();
                            data.putExtra(ACInsertAdPictureChooser.EDITTED_IMG_PATH, croppedFilePath);
                            setResult(RESULT_OK, data);
                        } else {
                            //fail back, just skip the crop picture
                            setResult(RESULT_FAILED);
                        }
                    } catch (NullPointerException nullPointer) {
                        //fail back, just skip the crop picture
                        setResult(RESULT_FAILED);
                        ACUtils.debug(nullPointer, false);
                    } catch (Exception anyException) {
                        setResult(RESULT_FAILED);
                    }

                    finish();
                    break;
                case R.id.button_rotate:
                    ACUtils.logCrashlytics("rotating picture");
                    try {
                        EventTrackingUtils.sendClick(XitiUtils.LEVEL2_INSERT_AD_ID, "Rotate Image", XitiUtils.NAVIGATION);
                        ACUtils.logCrashlytics("Rotate Image");
                        cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
                    } catch (NullPointerException nullPointer) {
                        //fail back, just skip the crop picture
                        setResult(RESULT_FAILED);
                        ACUtils.debug(nullPointer, false);
                        finish();
                    } catch (OutOfMemoryError oom) {
                        ACUtils.debugMemory(oom, "EditPicture_memory_rotate", Constants.EMPTY_STRING);
                        setResult(RESULT_FAILED);
                        finish();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d();
        ACUtils.unbindDrawables(findViewById(R.id.edit_picture_layout));
        if (cropImageView != null && cropImageView.getBackground() != null) {
            cropImageView.getBackground().setCallback(null);
        }
        if (croppedImage != null && !croppedImage.isRecycled()) {
            croppedImage.recycle();
            croppedImage = null;
        }
        System.gc();
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_picture);

        // Initialize components of the app
        cropImageView = (CropImageView) findViewById(R.id.CropImageView);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        //disable hardware acceleration for the CroppedImageView other the rotation will not be saved correctly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            cropImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            selectedImgPath = bundle.getString(ACInsertAdPictureChooser.SELECTED_IMG_PATH);
            Log.d("from bundle selectedImgPath: " + selectedImgPath);
        } else if (savedInstanceState != null && savedInstanceState.containsKey("selectedImgPath")) {
            Log.d("restore back selectedImgPath: " + selectedImgPath);
            selectedImgPath = savedInstanceState.getString("selectedImgPath");
        }

        if (!ACUtils.isEmpty(selectedImgPath)) {
            ACUtils.logCrashlytics("EditPicture_imgPath: " + selectedImgPath);

            cropImageView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {
                            new DisplayImageTask(EditPictureActivity.this, cropImageView, cropImageView.getWidth(), cropImageView.getHeight()).execute(selectedImgPath);
                            // Now you can get rid of this listener
                            try {
                                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                            } catch (IllegalStateException illegal) {
                                ACUtils.debug(illegal);
                            }
                            return true;
                        }
                    });
        } else {
            setResult(RESULT_FAILED);
            finish();
        }

        //Sets the rotate button
        final ImageView rotateView = (ImageView) findViewById(R.id.button_rotate);
        rotateView.setOnClickListener(editImgOnClickListener);

        final Button cropButton = (Button) findViewById(R.id.btn_crop);
        cropButton.setOnClickListener(editImgOnClickListener);

        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private String createCroppedFile() {
        DateFormat inputDF = new SimpleDateFormat("mmddyymmss");
        Date curDate = new Date();
        String croppedFilePath = Constants.EMPTY_STRING;

        File mFileTemp = FileUtils.getPublicTempImageFile(Config.UPLOAD_IMAGES_DIR, inputDF.format(curDate) + Config.CROP_IMG_NAME);
        try {
            mFileTemp.createNewFile();
            croppedFilePath = mFileTemp.getPath();
            ACUtils.logCrashlytics("Created " + croppedFilePath);
        } catch (IOException e) {
            ACUtils.debug(e);
        }

        return croppedFilePath;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("selectedImgPath", selectedImgPath);
    }

    private static class DisplayImageTask extends AsyncTask<String, Void, Bitmap> {
        WeakReference<EditPictureActivity> activityWeakReference;
        WeakReference<CropImageView> imageViewReference;
        int imageViewWidth, imageViewHeight;

        public DisplayImageTask(EditPictureActivity editPictureActivity, CropImageView params, int viewWidth, int viewHeight) {
            activityWeakReference = new WeakReference<>(editPictureActivity);
            imageViewReference = new WeakReference<>(params);
            imageViewWidth = viewWidth;
            imageViewHeight = viewHeight;
        }

        protected Bitmap doInBackground(String... selectedImgPath) {
            if (activityWeakReference == null || activityWeakReference.get() == null || ACUtils.isEmpty(selectedImgPath[0]))
                return null;
            EditPictureActivity editPictureActivity = activityWeakReference.get();
            Bitmap selectedBitmap = null;
            try {
                ACUtils.logCrashlytics("EditPicture displaying image: " + imageViewWidth + " x " + imageViewHeight);
                selectedBitmap = ImageUtils.getBitmapWithinOption(editPictureActivity, selectedImgPath[0], imageViewWidth, imageViewHeight, ApiConfigs.getMaxWidth(), ApiConfigs.getMaxHeight());

            } catch (Exception anyException) {
                editPictureActivity.setResult(RESULT_FAILED);
            }
            return selectedBitmap;
        }

        protected void onPostExecute(Bitmap selectedBitmap) {
            if (activityWeakReference == null || activityWeakReference.get() == null)
                return;

            EditPictureActivity editPictureActivity = activityWeakReference.get();

            if (selectedBitmap == null) {
                editPictureActivity.setResult(RESULT_FAILED);
                editPictureActivity.finish();
            }
            if (imageViewReference != null) {
                CropImageView imageView = imageViewReference.get();
                editPictureActivity.pbLoading.setVisibility(View.GONE);
                imageView.setImageBitmap(selectedBitmap);
                Log.logHeap();
            }
        }
    }

}
