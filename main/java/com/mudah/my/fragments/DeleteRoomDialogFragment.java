package com.mudah.my.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import com.mudah.my.R;
import com.mudah.my.activities.InboxActivity;
import com.mudah.my.utils.MudahUtil;


public class DeleteRoomDialogFragment extends DialogFragment {

    public DeleteRoomDialogFragment() {

    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                .setTitle(R.string.delete_popup_title)
                .setMessage(R.string.delete_chat_room)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StatusDelete status = new StatusDelete();
                        status.setIsStatusDelete(true);
                        if (getActivity() instanceof InboxActivity) {
                            ((InboxActivity) getActivity()).onRetrieveDelete(status);
                        }
                        DeleteRoomDialogFragment.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }
                        return true;
                    }
                });

        AlertDialog dialog = builder.show();
        MudahUtil.hideDialogDivider(getContext(), dialog);

        return dialog;
    }

    public class StatusDelete {
        private boolean isStatusDelete;

        public boolean isStatusDelete() {
            return isStatusDelete;
        }

        public void setIsStatusDelete(boolean isStatusDelete) {
            this.isStatusDelete = isStatusDelete;
        }
    }
}

