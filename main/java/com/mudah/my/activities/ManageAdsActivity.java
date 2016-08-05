package com.mudah.my.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.comscore.analytics.comScore;
import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.helpers.AnimationHelper;

import java.lang.ref.WeakReference;

public class ManageAdsActivity extends AppCompatActivity {
    private static final boolean ANIMATE = true;
    private static final int MAX_RELOAD = 3;
    private static final String MANAGEAD_URLPATH = "/ai";
    private static final String INSERT_URLPATH = "/ai/form";
    private String fullManageAdUrl = "";
    private WebView mManageAdsWebView;
    private WeakReference<ManageAdsActivity> wrActivity;
    private ProgressBar pbLoading;
    private int reloadAttempt = 0;
    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView wv, String strUrl) {
            Uri url = null;
            boolean notSupportedFeature = false;
            try {
                url = Uri.parse(strUrl);
                Log.d("url=" + strUrl + ", path=" + url.getPath());
            } catch (Exception e) {
                url = null;
            }

            if (url != null && url.getPath().contains(INSERT_URLPATH)) {
                notSupportedFeature = true;
                //startActivity(new Intent(ManageAdsActivity.this, InsertAdWebViewActivity.class));
                //return false;
            } else if (url != null && url.getPath().contains(MANAGEAD_URLPATH)) {
                // Only load in this webview if its the same host and only on
                // manage ad pages(/ai)
                return false;
            }

            if (wrActivity != null && !isFinishing()) {
                // Otherwise, send an intent request to open up new browser for outside links(outside insert ad)
                OpenBrowserDialog dialog = OpenBrowserDialog.newInstance(strUrl, notSupportedFeature);//new OpenBrowserDialog(url);
                // http://stackoverflow.com/questions/14177781/java-lang-illegalstateexception-can-not-perform-this-action-after-onsaveinstanc
                dialog.show(wrActivity.get().getSupportFragmentManager(),
                        "open_browser_dialog");

            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.e("errorCode= " + errorCode + ", failingUrl= " + failingUrl + ", reloadAttempt= " + reloadAttempt + "/ " + MAX_RELOAD);
            // if ERROR_CONNECT, try again
            if (errorCode == WebViewClient.ERROR_CONNECT
                    && mManageAdsWebView != null && reloadAttempt < MAX_RELOAD) {
                mManageAdsWebView.loadUrl(fullManageAdUrl);
                reloadAttempt++;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnimationHelper animate = new AnimationHelper(this, AnimationHelper.ACTIVITY_TRANSITION_OPEN);
        animate.run();

        // http://stackoverflow.com/questions/14177781/java-lang-illegalstateexception-can-not-perform-this-action-after-onsaveinstanc
        wrActivity = new WeakReference<ManageAdsActivity>(this);
        setContentView(R.layout.activity_manage_ads);

        mManageAdsWebView = (WebView) findViewById(R.id.activity_manage_ads);
        mManageAdsWebView.getSettings().setJavaScriptEnabled(true);
        mManageAdsWebView.setWebViewClient(webViewClient);
        mManageAdsWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100)
                    setLoadingVisible(false);
            }
        });
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        setLoadingVisible(true);
        if (savedInstanceState != null && savedInstanceState.getString("fullManageAdUrl") != null) {
            fullManageAdUrl = savedInstanceState.getString("fullManageAdUrl");
        } else {
            fullManageAdUrl = getIntent().getData().toString();
        }

        if (!ACUtils.isEmpty(fullManageAdUrl)) {
            Log.d("fullManageAdUrl= " + fullManageAdUrl + ", Config.manageAdURL= " + Config.manageAdURL);
            if (fullManageAdUrl.contains(Config.manageAdURL)) {
                mManageAdsWebView.getSettings().setUserAgentString(Config.getUserAgent(this));
                Log.d(" user agent= " + Config.androidUA);
                mManageAdsWebView.loadUrl(fullManageAdUrl);
            }
        } else
            startActivity(new Intent(this, AdsListActivity.class));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, AdsListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mManageAdsWebView != null)
            mManageAdsWebView.saveState(outState);
        if (fullManageAdUrl != null) {
            outState.putString("fullManageAdUrl", fullManageAdUrl);
        }
    }

    private void setLoadingVisible(boolean visible) {
        if (pbLoading != null && mManageAdsWebView != null) {
            if (!visible) {
                if (ANIMATE) {
                    pbLoading.startAnimation(AnimationUtils.loadAnimation(this,
                            android.R.anim.fade_out));
                    mManageAdsWebView.startAnimation(AnimationUtils
                            .loadAnimation(this, android.R.anim.fade_in));
                } else {
                    pbLoading.clearAnimation();
                    mManageAdsWebView.clearAnimation();
                }
                pbLoading.setVisibility(View.GONE);
                mManageAdsWebView.setVisibility(View.VISIBLE);
                // In old android version, soft keyboard does not pop up if
                // Webview is not focus.
                mManageAdsWebView.requestFocus(View.FOCUS_DOWN);
            } else {
                if (ANIMATE) {
                    pbLoading.startAnimation(AnimationUtils.loadAnimation(this,
                            android.R.anim.fade_in));
                    mManageAdsWebView.startAnimation(AnimationUtils
                            .loadAnimation(this, android.R.anim.fade_out));
                } else {
                    pbLoading.clearAnimation();
                    mManageAdsWebView.clearAnimation();
                }
                pbLoading.setVisibility(View.VISIBLE);
                mManageAdsWebView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mManageAdsWebView != null)
            mManageAdsWebView.restoreState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Notify comScore about lifecycle usage
        comScore.onExitForeground();
        AnimationHelper animate = new AnimationHelper(this, AnimationHelper.ACTIVITY_TRANSITION_CLOSE);
        animate.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Notify comScore about lifecycle usage
        comScore.onEnterForeground();
    }

    public static class OpenBrowserDialog extends RetainedDialogFragment {

        public static OpenBrowserDialog newInstance(String url, boolean notSupportedFeature) {
            OpenBrowserDialog frag = new OpenBrowserDialog();
            Bundle args = new Bundle();
            args.putString("url", url);
            args.putBoolean("notSupportedFeature", notSupportedFeature);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String url = getArguments().getString("url");
            final boolean notSupportedFeature = getArguments().getBoolean("notSupportedFeature");
            int messageId = R.string.open_browser_message;
            if (notSupportedFeature)
                messageId = R.string.open_browser_message2;
            return new AlertDialog.Builder(getActivity())
                    .setCancelable(true)
                    .setTitle(R.string.open_browser_title)
                    .setMessage(messageId)
                    .setPositiveButton(R.string.open_browser_btn,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Intent intent = new Intent(
                                            Intent.ACTION_VIEW, Uri.parse(url));
                                    getActivity().startActivity(intent);
                                }
                            }
                    )
                    .setNegativeButton(
                            R.string.dialog_alert_failure_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                }
                            }
                    ).create();
        }
    }

}
