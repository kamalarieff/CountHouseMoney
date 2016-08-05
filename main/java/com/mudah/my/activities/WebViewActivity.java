package com.mudah.my.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lib701.datasets.ACReferences;
import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.utils.ACUtils;
import com.lib701.utils.FileUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.utils.AmplitudeUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends MudahBaseActivity {
    public static final String EXTERNAL_URL = "externalUrl";
    private static final int MAX_RELOAD = 3;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final String MUDAH_IMAGES_PREFIX = "mudah_img_";
    private static final String MUDAH_IMAGES_SUFFIX = ".jpg";
    private static final boolean ANIMATE = true;
    private static final String PATH_INSERT_AD = "/ai";
    private static final String PATH_VIEW_AD = "/view";
    private static final String PATH_LIST = "/list";
    //http://stackoverflow.com/questions/14177781/java-lang-illegalstateexception-can-not-perform-this-action-after-onsaveinstanc
    private static WeakReference<WebViewActivity> wrActivity = null;
    private WebView mWebView;
    private ValueCallback<Uri> mUploadMessage;
    private File imgFile;
    private Uri mCapturedImageURI = null;
    private ProgressBar pbLoading;
    private View vConnectionLost;
    private WebChromeClient webChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            if (progress == 100) {
                if (mWebView != null && mWebView.getVisibility() == View.GONE) {
                    setLoadingVisible(false);
                } else if (pbLoading != null) {
                    pbLoading.setVisibility(View.GONE);
                }
            } else {
                if (mWebView != null && pbLoading != null && mWebView.getVisibility() == View.VISIBLE) {
                    pbLoading.setVisibility(View.VISIBLE);
                }
            }
        }

        // to be called by Android 2.x
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            popUpFileChooser();
        }

        // to be called by Android 3.0 - 4.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            popUpFileChooser();
        }

        // to be called by Android 4.1 upwards
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            popUpFileChooser();
        }

        private void popUpFileChooser() {
            // Taking photo from camera
            // Creating Directory and File
            File imgDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), Config.UPLOAD_IMAGES_DIR);

            if (!imgDir.exists())
                imgDir.mkdirs();

            imgFile = new File(imgDir + File.separator + MUDAH_IMAGES_PREFIX + String.valueOf(System.currentTimeMillis()) + MUDAH_IMAGES_SUFFIX);
            mCapturedImageURI = Uri.fromFile(imgFile);

            // Getting all the possible apps/activities that can handle camera requests
            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final List<ResolveInfo> listCam = getPackageManager().queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                cameraIntents.add(intent);
            }

            // Select photo from gallery
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            fileIntent.setType(FileUtils.MIME_TYPE_IMAGE);

            // Send an intent to all applications than handle camera and gallery
            Intent chooserIntent = Intent.createChooser(fileIntent, "Upload Images");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView childView = new WebView(view.getContext());
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }

    };
    private int reloadAttempt = 0;
    private String externalUrl = Config.DEFAULT_SAFTY_LINK;
    private WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("url: " + url);
            Uri parseUrl = Uri.parse(url);
            if (parseUrl.getHost().contains(Config.host)) {
                ACReferences ref = ACReferences.getACReferences();
                // Only load in this webview if its the same host and only on insert ad pages(/ai)
                if (PATH_INSERT_AD.equalsIgnoreCase(parseUrl.getPath())) {
                    Intent intentInsert = new Intent(WebViewActivity.this, InsertAdActivity.class);
                    intentInsert.putExtra(InsertAdActivity.SUFFIX, true);
                    intentInsert.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentInsert);
                    return true;
                } else if (PATH_VIEW_AD.equalsIgnoreCase(parseUrl.getPath())) {
                    // example http://m.muddah.com/view?f=p&ad_id=45501573
                    ArrayList<String> listIdList = new ArrayList<>();
                    listIdList.add(parseUrl.getQueryParameter(Constants.AD_ID));

                    Intent intentView = new Intent(WebViewActivity.this, AdViewActivity.class);
                    intentView.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentView.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, 0);
                    intentView.putExtra(AdViewActivity.EXTRA_GRAND_TOTAL, 1);
                    intentView.putExtra(AdViewActivity.VIEW_TYPE, AmplitudeUtils.AD_TYPE_R4U);
                    intentView.putStringArrayListExtra(AdViewActivity.EXTRA_ALL_LIST_ID, listIdList);
                    startActivity(intentView);
                    return true;
                } else if (PATH_LIST.equalsIgnoreCase(parseUrl.getPath())) {
                    // example recommended category: http://m.muddah.com/list?q=&ca=9_1_s&cg=1000&o=1&f=p&srch=1&so=1 
                    // example deal near you: http://m.muddah.com/list?q=&ca=8_1_s&cg=&catname=All+Categories&o=1&f=p&srch=1&so=1
                    String categoryId = parseUrl.getQueryParameter("cg");
                    String regionString = parseUrl.getQueryParameter("ca");
                    Log.d("recommended cg: " + categoryId + ", ca: " + regionString);
                    Intent intent = null;
                    if (!ACUtils.isEmpty(categoryId)) {
                        intent = new Intent(WebViewActivity.this, AdsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra(AdsListActivity.VIEW_TYPE, AmplitudeUtils.AD_TYPE_R4U);
                        intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_CATEGORY);

                        ref.categoryId = categoryId;
                    } else if (!ACUtils.isEmpty(regionString)) {
                        String regionId = regionString.split(Constants.UNDERSCORE)[0];
                        ref.setCategoryGroupId(Constants.EMPTY_STRING);
                        ref.setCategoryId(null);
                        ref.setRegionId(regionId);
                        intent = new Intent(WebViewActivity.this, AdsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra(AdsListActivity.VIEW_TYPE, AmplitudeUtils.AD_TYPE_R4U);
                        intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_CATEGORY_LOCATION);
                    }

                    if (intent != null) {
                        startActivity(intent);
                    }
                    return true;
                } else {
                    Config.preferLang = parseUrl.getQueryParameter("lang");
                    if (!ACUtils.isEmpty(Config.preferLang)) {
                        Log.d("preferLang: " + Config.preferLang);
                        PreferencesUtils.getSharedPreferences(WebViewActivity.this).edit()
                                .putString(PreferencesUtils.PREFER_LANG, Config.preferLang)
                                .apply();
                    }

                }
                //To allow the page to stay in Webview and display a new page
                return false;
            }
            if (wrActivity != null && !isFinishing()) {
                // Otherwise, send an intent request to open up new browser for outside links(outside insert ad)
                OpenBrowserDialog dialog = OpenBrowserDialog.newInstance(url);
                //http://stackoverflow.com/questions/14177781/java-lang-illegalstateexception-can-not-perform-this-action-after-onsaveinstanc
                dialog.show(wrActivity.get().getSupportFragmentManager(), "open_browser_dialog");
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.e("errorCode= " + errorCode + ", failingUrl= " + failingUrl + ", reloadAttempt= " + reloadAttempt + "/ " + MAX_RELOAD);
            // if ERROR_CONNECT, try again
            if (errorCode == WebViewClient.ERROR_CONNECT
                    && mWebView != null && reloadAttempt < MAX_RELOAD) {
                mWebView.loadUrl(externalUrl);
                reloadAttempt++;
            } else {
                vConnectionLost.startAnimation(AnimationUtils.loadAnimation(
                        WebViewActivity.this, android.R.anim.fade_in));
                vConnectionLost.setVisibility(View.VISIBLE);
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        String url = mWebView.getUrl();
        Log.d("loaded url= " + url);
        if (ACUtils.isEmpty(url) && mWebView != null) {
            mWebView.getSettings().setUserAgentString(Config.getUserAgent(this));

            if (!ACUtils.isEmpty(externalUrl)) {
                updateLoading();
                Log.d("loading URL: " + externalUrl + ", user agent= " + Config.androidUA);
            } else {
                vConnectionLost.setVisibility(View.GONE);
                mWebView.loadUrl(Config.insertAdURL);
                Log.d("loading URL:" + Config.insertAdURL + ", user agent= " + Config.androidUA);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vConnectionLost != null) {
            ACUtils.unbindDrawables(vConnectionLost);
        }
    }

    private void updateLoading() {
        vConnectionLost.setVisibility(View.GONE);
        mWebView.loadUrl(externalUrl);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        //http://stackoverflow.com/questions/14177781/java-lang-illegalstateexception-can-not-perform-this-action-after-onsaveinstanc
        wrActivity = new WeakReference<WebViewActivity>(this);
        setContentView(R.layout.activity_webview);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);

        vConnectionLost = findViewById(R.id.web_connection_lost);
        ImageView connectionLostImg = (ImageView) findViewById(R.id.imgv_connection_lost);
        Picasso.with(getApplicationContext()).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);

        vConnectionLost.setVisibility(View.GONE);
        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoading();
            }
        });

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        mWebView = (WebView) findViewById(R.id.insert_ad_webview);
        setLoadingVisible(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUseWideViewPort(true);
        //fixes: cannot detect click inside the iframe (panel recruitment issue)
        //support multiple windows so that is will call the onCreateWindow to open up the browser
        //http://stackoverflow.com/questions/14989410/android-intercept-click-iframe-link-adbanner
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.addJavascriptInterface(new WebAppInterface(this), Config.appId);
        mWebView.setWebViewClient(webViewClient);
        //reference from http://stackoverflow.com/questions/10953957/webview-android-4-0-file-upload
        mWebView.setWebChromeClient(webChromeClient);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(EXTERNAL_URL)) {
            externalUrl = bundle.getString(EXTERNAL_URL);
        } else if (getIntent() != null && getIntent().getData() != null) {
            String internalDeepLinkUrl = getIntent().getData().toString();
            //there are web links for tips and rules on the insert ad form which are invoked in app activity
            //the format of the links is com.mudah.my://android.intent.action.VIEW/http://www.mudah.my/about/index.htm?ca=9_s&page=rules
            //So need to extract a right url from that format
            if (internalDeepLinkUrl.contains("com.mudah.my")) {
                int index = internalDeepLinkUrl.indexOf("http");
                if (index > -1)
                    externalUrl = internalDeepLinkUrl.substring(index);
            }
        } else if (savedInstanceState != null && savedInstanceState.containsKey(EXTERNAL_URL)) {
            externalUrl = savedInstanceState.getString(EXTERNAL_URL);
        }

        if (!ACUtils.isEmpty(externalUrl)) {
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setUserAgentString(Config.getUserAgent(this));
            mWebView.loadUrl(externalUrl);
            Log.d("loading URL: " + externalUrl + ", user agent= " + Config.androidUA);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage == null)
                return;

            Uri result = null;
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    if (imgFile.exists()) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, imgFile.getAbsolutePath());
                        result = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    }
                } else {
                    result = intent.getData();
                }
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setLoadingVisible(boolean visible) {
        if (pbLoading != null && mWebView != null) {
            if (!visible) {
                if (ANIMATE) {
                    pbLoading.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                    mWebView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                } else {
                    pbLoading.clearAnimation();
                    mWebView.clearAnimation();
                }
                pbLoading.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                //In old android version, soft keyboard does not pop up if Webview is not focus.
                mWebView.requestFocus(View.FOCUS_DOWN);
            } else {
                if (ANIMATE) {
                    pbLoading.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                    mWebView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                } else {
                    pbLoading.clearAnimation();
                    mWebView.clearAnimation();
                }
                pbLoading.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWebView != null)
            mWebView.saveState(outState);

        if (externalUrl != null) {
            outState.putString("externalUrl", externalUrl);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mWebView != null)
            mWebView.restoreState(savedInstanceState);
    }

    public static class OpenBrowserDialog extends RetainedDialogFragment {
        public static OpenBrowserDialog newInstance(String url) {
            OpenBrowserDialog frag = new OpenBrowserDialog();
            Bundle args = new Bundle();
            args.putString("url", url);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String url = getArguments().getString("url");

            return new AlertDialog.Builder(getActivity())
                    .setCancelable(true)
                    .setTitle(R.string.open_browser_title)
                    .setMessage(R.string.open_browser_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            getActivity().startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.dialog_alert_failure_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                        }
                    })
                    .create();
        }
    }

    public class WebAppInterface {
        Context mContext;

        public WebAppInterface(Context c) {
            mContext = c;
        }

        // Show a toast from the webpage
        public void showToastMsg(String msg) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

}
