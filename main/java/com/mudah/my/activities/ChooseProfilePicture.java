package com.mudah.my.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.datasets.ACReferences;
import com.lib701.utils.ACUtils;
import com.lib701.utils.FileUtils;
import com.lib701.utils.ImageUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.connection.MudahACBlocketConnection;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by kalpana on 5/18/16.
 */
public class ChooseProfilePicture extends FragmentActivity {
    public static final String RESULT_IMAGE_URL = "image_id";
    public static final String RESULT_FILE_NAME = "thumbnail_digest";
    public static final String RESULT_STATUS = "status";
    public static final String RESULT_ERROR_MESSAGE = "error_message";
    public static final String EXTRA_CHANGE = "change";
    public static final String LAST_IMAGE_POSITION = "last_image_position";
    public static final String SELECTED_IMG_PATH = "selected_img_path";
    public static final String EDITTED_IMG_PATH = "edit_img_path";
    private static final int REQUEST_TAKE = 1;
    private static final int REQUEST_BROWSE = 2;
    private static final int REQUEST_CROP = 3;
    private static final int LOADER_UPLOAD_BITMAP = 1;
    private TextView takePicture;
    private TextView browseImage;
    private Uri selectedImage;
    private ProgressBar pbLoading;
    private int maxWidth = 640;
    private int maxHeight = 480;
    private int minWidth = 0;
    private int minHeight = 0;
    private Boolean change = false;
    private ACReferences ref;
    private int selectedPosition = 0;
    private String croppedImgPath;
    private ImageView cameraImageIcon;
    private ImageView galleryImageIcon;

    private View.OnClickListener browseImageListener = new View.OnClickListener() {
        public void onClick(View v) {
            selectedImage = null;//reset this to null
            try {
                Intent intent;
                //Kitkat sdk 4.4 has changed a return URI format
                //Ref: http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                }
                intent.setType(FileUtils.MIME_TYPE_IMAGE);
                startActivityForResult(intent, REQUEST_BROWSE);
            } catch (ActivityNotFoundException noActivity) {
                Toast.makeText(ChooseProfilePicture.this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener takePictureListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File camFile = FileUtils.getPublicTempImageFile(Config.UPLOAD_IMAGES_DIR, Config.INSERT_AD_IMG_NAME);
            try {
                camFile.createNewFile();
                selectedImage = Uri.fromFile(camFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                startActivityForResult(intent, REQUEST_TAKE);
            } catch (IOException e) {
                Log.e("File Creation error " + e.toString());
                Toast.makeText(ChooseProfilePicture.this, R.string.insert_ad_picture_write_error, Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String selectedImgPath = Constants.EMPTY_STRING;
        boolean sendToAPI = false;
        Log.d("requestCode: " + requestCode + ", resultCode: " + resultCode);
        // finish image upload if the result is cancel
        // if the result is not ok for the crop, still proceed with uploading the normal picture (without crop)
        if (resultCode == RESULT_CANCELED || (resultCode != RESULT_OK && requestCode != REQUEST_CROP)) {
            return;
        }
        switch (requestCode) {
            case REQUEST_BROWSE:
                if (data != null) {
                    Log.d("Image got from file ");

                    selectedImage = data.getData();
                    if (selectedImage != null)
                        selectedImgPath = FileUtils.getPath(ChooseProfilePicture.this, selectedImage);

                    Log.d("Final path " + selectedImgPath);
                    if (!editImage(selectedImgPath))
                        sendToAPI = true;//just skip the editting part if fail
                }
                break;
            case REQUEST_TAKE:
                Log.d("Image captured from camera, selectedImage: " + selectedImage);
                if (selectedImage != null) {
                    if (!editImage(selectedImage.getPath())) {
                        sendToAPI = true;//just skip the editting part if fail
                    }
                }
                break;
            case REQUEST_CROP:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        selectedImgPath = bundle.getString(EDITTED_IMG_PATH);
                        Log.d("After croping ImgPath: " + selectedImgPath);
                        selectedImage = Uri.fromFile(new File(selectedImgPath));
                        croppedImgPath = selectedImgPath;
                    }
                }
                sendToAPI = true;
                break;
        }

        Log.d("requestCode: " + requestCode + ", sendToAPI: " + sendToAPI);
        if (sendToAPI) {
            if (selectedImage != null) {
                getSupportLoaderManager().restartLoader(LOADER_UPLOAD_BITMAP, null, newUploadBitmapCallbacks(selectedImage));
            } else {
                String imageTooSmallMsg = getResources().getString(R.string.insert_ad_validation_image_too_small, String.valueOf(minWidth), String.valueOf(minHeight));
                Toast.makeText(ChooseProfilePicture.this, imageTooSmallMsg, Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    private void hideAllImageOptions(){
        takePicture.setVisibility(View.INVISIBLE);
        cameraImageIcon.setVisibility(View.INVISIBLE);

        browseImage.setVisibility(View.INVISIBLE);
        galleryImageIcon.setVisibility(View.INVISIBLE);
    }

    private LoaderManager.LoaderCallbacks<Intent> newUploadBitmapCallbacks(final Uri selectedImage) {
        pbLoading.setVisibility(View.VISIBLE);
        hideAllImageOptions();
        return new LoaderManager.LoaderCallbacks<Intent>() {

            @Override
            public void onLoaderReset(Loader<Intent> loader) {
            }

            @Override
            public void onLoadFinished(Loader<Intent> loader, Intent data) {
                if (data != null) {
                    getSupportLoaderManager().destroyLoader(loader.getId());
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(ChooseProfilePicture.this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public Loader<Intent> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Intent>(ChooseProfilePicture.this) {
                    private boolean dataIsReady = false;
                    private Intent data;

                    @Override
                    protected void onStartLoading() {
                        if (dataIsReady) {
                            deliverResult(data);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public Intent loadInBackground() {
                        dataIsReady = false;
                        String filePath;
                        String errorMessage = null;
                        if (selectedImage != null && "content".equals(selectedImage.getScheme())) {
                            filePath = FileUtils.getPath(ChooseProfilePicture.this, selectedImage);
                        } else if (selectedImage != null && "file".equals(selectedImage.getScheme())) {
                            // convert file:// URI to filePath
                            filePath = selectedImage.getPath();
                        } else {
                            data = null;
                            dataIsReady = true;
                            return data;
                        }
                        try {

                            if (ACUtils.isEmpty(filePath)) {
                                Log.d("display from URI");
                                ref.cachedImage = Picasso.with(ChooseProfilePicture.this)
                                        .load(selectedImage)
                                        .resize(maxWidth, maxHeight).centerInside().onlyScaleDown()
                                        .get();

                            } else {
                                Log.d("display from filePath: " + filePath);
                                ref.cachedImage = ImageUtils.getBitmapWithinOption(ChooseProfilePicture.this, filePath, maxWidth, maxHeight, maxWidth, maxHeight);
                            }
                        } catch (IOException e) {
                            ACUtils.debug(e);
                        }

                        //Check if image is still bigger than the minimum width and minimum height
                        if (ref.cachedImage != null && !ImageUtils.isBitmapWithinRange(ref.cachedImage, minWidth, minHeight)) {
                            errorMessage = getResources().getString(R.string.insert_ad_validation_image_too_small, String.valueOf(minWidth), String.valueOf(minHeight));
                        }

                        String imageUrl = null;
                        String fileName = null;
                        String status = null;
                        Integer quality = 75;
                        if (ref.cachedImage != null && ACUtils.isEmpty(errorMessage)) {
                            int imageSize = maxHeight * maxWidth;

                            while (imageSize >= maxHeight * maxWidth && quality > 5) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                ref.cachedImage.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                                ACReferences.cachedImageInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                                byte[] imageOutput = byteArrayOutputStream.toByteArray();
                                imageSize = imageOutput.length;
                                quality -= 5;
                            }

                            if (ACReferences.cachedImageInputStream != null) {
                                // upload image
                                MudahACBlocketConnection.ImageInfoModel imageInfoModel = MudahACBlocketConnection.profileImageinfo();
                                imageUrl = imageInfoModel.imageUrl;
                                fileName = imageInfoModel.imageDigest;
                                status = imageInfoModel.status;
                                errorMessage = imageInfoModel.errorMessage;
                                ref.setUploadedImage(true);
                                ACUtils.logCrashlytics(" imageUrl after uploading: " + imageUrl);
                            }
                            //free out the object
                            if (ref.cachedImage != null) {
                                ref.cachedImage.recycle();
                            }
                        }

                        ref.cachedImage = null;
                        if (ACUtils.isEmpty(imageUrl) && ACUtils.isEmpty(errorMessage)) {
                            errorMessage = getString(R.string.insert_ad_image_error_message);
                        }
                        Intent intent = new Intent(ChooseProfilePicture.this, EditUserProfileActivity.class);
                        intent.putExtra(RESULT_IMAGE_URL, imageUrl);
                        intent.putExtra(RESULT_FILE_NAME, fileName);
                        intent.putExtra(RESULT_STATUS, status);
                        intent.putExtra(RESULT_ERROR_MESSAGE, errorMessage);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        if (change) {
                            intent.putExtra(EXTRA_CHANGE, true);
                            intent.putExtra(LAST_IMAGE_POSITION, selectedPosition);
                        }

                        data = intent;
                        dataIsReady = true;
                        return intent;
                    }
                };
            }
        };
    }

    private boolean editImage(String selectedImgPath) {
        boolean result = false;
        try {
            Log.d("Starting crop img: " + selectedImgPath);
            if (!ACUtils.isEmpty(selectedImgPath)) {
                Intent editIntent = new Intent(this, EditPictureActivity.class);
                editIntent.putExtra(SELECTED_IMG_PATH, selectedImgPath);
                startActivityForResult(editIntent, REQUEST_CROP);
                result = true;
            }
        } catch (Exception allException) {
            //fail over
            Log.e("All other error,skip the crop feature ! " + allException);
            result = false;
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profile_picture);
        getWindow().setBackgroundDrawable(null);

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        pbLoading.setVisibility(View.GONE);

        ref = ACReferences.getACReferences();

        takePicture = (TextView) findViewById(R.id.take_picture);
        takePicture.setOnClickListener(takePictureListener);
        cameraImageIcon = (ImageView) findViewById(R.id.ic_camera);

        browseImage = (TextView) findViewById(R.id.browse_gallery);
        browseImage.setOnClickListener(browseImageListener);
        galleryImageIcon = (ImageView) findViewById(R.id.ic_gallery);

        if (getSupportLoaderManager().getLoader(LOADER_UPLOAD_BITMAP) != null) {
            getSupportLoaderManager().initLoader(LOADER_UPLOAD_BITMAP, null, newUploadBitmapCallbacks(null));
        }

        if (savedInstanceState != null && savedInstanceState.containsKey("savedSelectedImageUri")) {
            selectedImage = Uri.parse(savedInstanceState.getString("savedSelectedImageUri"));
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selectedImage != null) {
            savedInstanceState.putString("savedSelectedImageUri", selectedImage.toString());
        }
    }
}
