package com.mudah.my.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.lib701.fragments.RetainedDialogFragment;
import com.mudah.my.R;
import com.mudah.my.activities.AdViewActivity;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.widgets.ClearableEditText;

public class DeleteAdEmailFragment extends RetainedDialogFragment {

    private String email;
    private ClearableEditText emailAd;


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                .setTitle(R.string.delete_ad_email_title)
                .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_delete_ad, null))
                //.setMessage(R.string.delete_ad_email_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        email = emailAd.getText().toString();
                        if (getActivity() instanceof AdViewActivity) {
                            ((AdViewActivity) getActivity()).sendDeleteEmailTmp(email);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        AlertDialog alertDialog = builder.show();
        emailAd = (ClearableEditText) alertDialog.findViewById(R.id.delete_email);
        MudahUtil.hideDialogDivider(getContext(), alertDialog);

        return alertDialog;
    }
}