package com.mudah.my.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.utils.ACUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.activities.SignUpIntroActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.utils.MudahUtil;

import java.util.Map;

public class InsertAdSuccessFragment extends RetainedDialogFragment {
    private String name;
    private String email;
    private String passw;
    private AlertDialog dialogBuilder;

    public static InsertAdSuccessFragment instantiate(String name, Map<String, Object> sentParams) {
        InsertAdSuccessFragment fragment = new InsertAdSuccessFragment();
        fragment.name = name;
        if (sentParams.containsKey(InsertAdActivity.EMAIL))
            fragment.email = (String) sentParams.get(InsertAdActivity.EMAIL);
        if (sentParams.containsKey(InsertAdActivity.PASSWORD))
            fragment.passw = (String) sentParams.get(InsertAdActivity.PASSWORD);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() instanceof InsertAdActivity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                    .setTitle(getString(R.string.insert_ad_success_title, name))
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            getActivity().finish();
                        }
                    })
                    .setPositiveButton(R.string.insert_ad_success_insert, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Need to add Handler. Otherwise, restarting an activity from a fragment, it will just kill the activity and not restart it.
                            (new Handler()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = getActivity().getIntent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    getActivity().overridePendingTransition(0, 0);
                                    getActivity().finish();
                                    startActivity(intent);
                                }
                            });
                        }
                    });
            builder.setNegativeButton(R.string.insert_ad_success_home, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent;
                    if (Config.userAccount.isLogin()) {
                        intent = new Intent(getActivity(), AdsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    } else {
                        intent = new Intent(getActivity(), SignUpIntroActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(SignUpIntroActivity.EXTRA_SIGN_UP_EMAIL, email);
                        intent.putExtra(SignUpIntroActivity.EXTRA_SIGN_UP_PASSW, passw);
                    }
                    startActivity(intent);
                    getActivity().finish();
                }
            });
            TextView textView = new TextView(getActivity());
            textView.setPadding(40, 10, 20, 10);
            textView.setMovementMethod(LinkMovementMethod.getInstance()); // To handle a link in message to be linkable
            textView.setText(ACUtils.getHtmlFromString(getString(R.string.insert_ad_success_message, ((InsertAdActivity) getActivity()).formatURLToInvokeInAppActivity(Config.rulesUrl))));//To handle HTML message
            if (Build.VERSION.SDK_INT <= 10)
                textView.setTextColor(Color.WHITE);
            builder.setView(textView);

            dialogBuilder = builder.create();

            return dialogBuilder;
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();

        MudahUtil.hideDialogDivider(getContext(), dialogBuilder);
    }
}
