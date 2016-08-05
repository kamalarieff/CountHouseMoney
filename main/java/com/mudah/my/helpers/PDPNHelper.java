package com.mudah.my.helpers;

import android.app.Dialog;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.MudahPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class to keep configs that will be stored as a static object and
 * retrieve from shared preferences if the static object does not return any value
 */
public class PDPNHelper {
    private static Boolean pdpnEnable = null;
    private static Map<String, String> pdpnQueues = null;
    private static String pdpnOptin = null;

    public static void setPDPNconf(JSONArray data, Context context) throws JSONException {
        if (data.length() > 0) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject object = data.getJSONObject(i);
                if (!object.isNull("enabled")) {
                    String strData = object.getString("enabled");
                    if (strData.equalsIgnoreCase("1") || strData.equalsIgnoreCase("true")) {
                        pdpnEnable = true;
                    } else if (strData.equalsIgnoreCase("0") || strData.equalsIgnoreCase("false") || strData.equalsIgnoreCase(Constants.EMPTY_STRING)) {
                        pdpnEnable = false;
                    }
                } else if (!object.isNull("queues")) {
                    JSONArray jsonArrayQueues = object.getJSONArray("queues");
                    pdpnQueues = new HashMap<String, String>();
                    for (int j = 0; j < jsonArrayQueues.length(); j++) {
                        JSONObject jsonObjectQueue = jsonArrayQueues.getJSONObject(j);
                        Iterator<String> iter = jsonObjectQueue.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            pdpnQueues.put(key, jsonObjectQueue.getString(key));
                        }
                    }
                } else if (!object.isNull("opt_in_value")) {
                    pdpnOptin = object.getString("opt_in_value");
                }
            }
        }

        PreferencesUtils
                .getSharedPreferences(context).edit()
                .putString(MudahPreferencesUtils.PDPN_SETTINGS, data.toString())
                .apply();

        Log.d("saved ApiConfig");
    }

    public static void setInitPDPNconf(Context context) throws JSONException {
        String pdpnSettingsJsonArray = PreferencesUtils
                .getSharedPreferences(context)
                .getString(MudahPreferencesUtils.PDPN_SETTINGS, null);
        if (pdpnSettingsJsonArray != null) {
            JSONArray jsonArrayPdpnSettings = new JSONArray(pdpnSettingsJsonArray);
            setPDPNconf(jsonArrayPdpnSettings, context);
        }
    }

    public static Boolean getPDPNEnable(Context context) throws JSONException {
        if (pdpnEnable == null) {
            Log.d("getPDPNEnable: retrieve PDPNHelper from shared preferences");
            setInitPDPNconf(context);
        }
        return pdpnEnable;
    }

    public static Map<String, String> getPDPNQueues(Context context) throws JSONException {
        if (pdpnQueues == null) {
            Log.d("getPDPNQueues: retrieve PDPNHelper from shared preferences");
            setInitPDPNconf(context);
        }
        return pdpnQueues;
    }

    public static String getPDPNoptinValue(Context context) throws JSONException {
        if (pdpnOptin == null) {
            Log.d("getPDPNoptinValue: retrieve PDPNHelper from shared preferences");
            setInitPDPNconf(context);
        }
        return pdpnOptin;
    }

    /**
     * Save emails that has optted in into shared preferences
     *
     * @param paramSent params sent to mail.py api
     * @throws org.json.JSONException
     */
    public static boolean saveOpttedInEmails(Map<String, Object> paramSent, Context context) throws JSONException {
        boolean emailExist = false;
        if (paramSent.containsKey("opt_in")) {
            String intOptIn = paramSent.get("opt_in").toString();
            if (intOptIn != null) {
                String email = (String) paramSent.get("email");
                String emailJSONArray = PreferencesUtils
                        .getSharedPreferences(context)
                        .getString(MudahPreferencesUtils.PDPN_EMAILS, "[]");

                if (!ACUtils.isEmpty(emailJSONArray)) {
                    JSONArray jsonArrayOpttedInEmails = new JSONArray(emailJSONArray);

                    for (int i = 0; i < jsonArrayOpttedInEmails.length(); i++) {
                        JSONObject json_object = jsonArrayOpttedInEmails.getJSONObject(i);
                        @SuppressWarnings("unchecked")
                        Iterator<String> iter = json_object.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            String emailAtIndex = jsonArrayOpttedInEmails.getJSONObject(Integer.parseInt(key)).getString(key);
                            if (emailAtIndex.equalsIgnoreCase(email)) {
                                emailExist = true;
                            }
                        }
                    }

                    if (!emailExist) {
                        JSONObject jsonObjectOpttedInEmail = new JSONObject();
                        String noEntries = String.valueOf(jsonArrayOpttedInEmails.length());
                        jsonObjectOpttedInEmail.put(noEntries, email.toLowerCase());

                        jsonArrayOpttedInEmails.put(jsonObjectOpttedInEmail);
                        String pdpnOptiontype = Constants.EMPTY_STRING;
                        if (paramSent.containsKey("queues")) {
                            pdpnOptiontype = paramSent.get("queues").toString();
                        }
                        PreferencesUtils
                                .getSharedPreferences(context).edit()
                                .putString(MudahPreferencesUtils.PDPN_EMAILS, jsonArrayOpttedInEmails.toString())
                                .putString(MudahPreferencesUtils.PDPN_OPT_IN_TYPE, pdpnOptiontype)
                                .apply();
                    }
                }
            }
        }
        return emailExist;
    }

    /**
     * Function to set the PDPN notice in the contact advertiser form
     */
    public static void setPDPNmessage(View view, Context context) {

        final Context contextPdpn = context;
        final int startHyperlink = 76;
        final int end_hyperlink = 107;
        String pdpaTnCMessage = context.getResources().getString(R.string.mail_dialog_pdpa_tnc_message);

        SpannableString ss = new SpannableString(pdpaTnCMessage);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    showMessage(contextPdpn);
                } catch (JSONException e) {
                    ACUtils.debug(e);
                }
            }
        };

        if (end_hyperlink > pdpaTnCMessage.length()) {
            ss.setSpan(clickableSpan, 0, end_hyperlink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ss.setSpan(clickableSpan, startHyperlink, end_hyperlink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        TextView textView = (TextView) view.findViewById(R.id.tv_pdpa_tnc_msg);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    /**
     * Function to perform the pop up dialog box containing PDPN messages
     *
     * @throws org.json.JSONException
     */
    public static void showMessage(Context context) throws JSONException {

        final Dialog dialog = new Dialog(context, R.style.TranslucentFadeDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_email_advertiser_message);

        WebView webViewEn = (WebView) dialog.findViewById(R.id.wv_messages);
        //String htmlEn = PDPNHelper.getPDPNMessage(context);
        webViewEn.loadUrl("file:///android_asset/pdpn_en.html");
        //webViewEn.loadData(htmlEn, "text/html", "UTF-8");

        WebView webViewBm = (WebView) dialog.findViewById(R.id.wv_messages_bm);
        //String htmlBm = PDPNHelper.getPDPNMessageBm(context);
        webViewBm.loadUrl("file:///android_asset/pdpn_bm.html");
        //webViewBm.loadData(htmlBm, "text/html", "UTF-8");

        TabHost tabs = (TabHost) dialog.findViewById(R.id.tabHost2);
        tabs.setup();
        TabHost.TabSpec tspecEn = tabs.newTabSpec("Tab EN");
        tspecEn.setIndicator("ENGLISH");
        tspecEn.setContent(R.id.tab_en);
        tabs.addTab(tspecEn);
        TabHost.TabSpec tspecBm = tabs.newTabSpec("Tab BM");
        tspecBm.setIndicator("BAHASA MALAYSIA");
        tspecBm.setContent(R.id.tab_bm);
        tabs.addTab(tspecBm);

        RelativeLayout rlOutside = (RelativeLayout) dialog.findViewById(R.id.rl_outside);
        rlOutside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_close);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Hide PDPN checkbox messages if email has already opted in in this device
     *
     * @throws org.json.JSONException
     */
    public static void hidePDPNmessages(Context context, String email, LinearLayout llPdpnCheckboxes) throws JSONException {

        boolean isEmailExisted = isEmailExisted(context, email);

        if (isEmailExisted) {
            llPdpnCheckboxes.setVisibility(View.GONE);
        } else {
            llPdpnCheckboxes.setVisibility(View.VISIBLE);
        }

    }

    public static void hidePDPNmessages(Context context, String email, CheckBox pdpnCheckboxes) throws JSONException {

        boolean isEmailExisted = isEmailExisted(context, email);

        if (isEmailExisted) {
            pdpnCheckboxes.setVisibility(View.GONE);
        } else {
            pdpnCheckboxes.setVisibility(View.VISIBLE);
        }

    }

    private static boolean isEmailExisted(Context context, String email) throws JSONException {
        String emailJSONArray = PreferencesUtils
                .getSharedPreferences(context)
                .getString(MudahPreferencesUtils.PDPN_EMAILS, "[]");

        if (emailJSONArray != null) {
            JSONArray jsonArrayOpttedInEmails = new JSONArray(emailJSONArray);
            for (int i = 0; i < jsonArrayOpttedInEmails.length(); i++) {
                JSONObject json_object = jsonArrayOpttedInEmails.getJSONObject(i);
                @SuppressWarnings("unchecked")
                Iterator<String> iter = json_object.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    String emailAtIndex = jsonArrayOpttedInEmails.getJSONObject(Integer.parseInt(key)).optString(key);

                    if (emailAtIndex.equalsIgnoreCase(email.trim())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getPdpnOptionType(Context context) {
        String pdpnOptionType = PreferencesUtils
                .getSharedPreferences(context)
                .getString(MudahPreferencesUtils.PDPN_OPT_IN_TYPE, Constants.EMPTY_STRING);
        return pdpnOptionType;
    }
}
