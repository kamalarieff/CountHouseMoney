package com.mudah.my.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mudah.my.R;
import com.mudah.my.helpers.ActionBarHelper;

public class TermsActivity extends MudahBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);

        WebView wvContent = (WebView) findViewById(R.id.wv_content);
        wvContent.loadUrl("file:///android_asset/terms.html");
        wvContent.setWebViewClient(new WebViewClient() {
            // Override URL
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getScheme().equalsIgnoreCase("mailto")) {
                    String email = Uri.parse(url).getSchemeSpecificPart();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    startActivity(intent);
                } else if (Uri.parse(url).getScheme().equalsIgnoreCase("tel")) {
                    String telephone = Uri.parse(url).getSchemeSpecificPart();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("tel:" + telephone));
                    startActivity(intent);
                } else if (Uri.parse(url).getScheme().equalsIgnoreCase("privacyPolicyActivity")) {
                    Intent intentPrivacy = new Intent(getBaseContext(), PrivacyPolicyActivity.class);
                    intentPrivacy.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentPrivacy);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
                return true;
            }
        });
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

}
