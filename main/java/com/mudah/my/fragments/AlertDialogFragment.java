package com.mudah.my.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.utils.ACUtils;
import com.mudah.my.R;
import com.mudah.my.utils.MudahUtil;

public class AlertDialogFragment extends RetainedDialogFragment {
    private String message;
    private String title;

    public static AlertDialogFragment instantiate(String title, String message, OnDismissListener dismissListener) {
        AlertDialogFragment frag = new AlertDialogFragment();
        frag.message = message;
        frag.title = title;
        frag.setOnDismissListener(dismissListener);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(title);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        TextView textView = new TextView(getActivity());
        textView.setPadding(30, 10, 20, 10);
        textView.setMovementMethod(LinkMovementMethod.getInstance()); // To handle a link in message to be linkable
        if (!ACUtils.isEmpty(message))
            textView.setText(ACUtils.getHtmlFromString(message));//To handle HTML message
        if (Build.VERSION.SDK_INT <= 10)
            textView.setTextColor(Color.WHITE);
        builder.setView(textView);

        AlertDialog dialog = builder.show();
        MudahUtil.hideDialogDivider(getContext(), dialog);

        return dialog;
    }
}