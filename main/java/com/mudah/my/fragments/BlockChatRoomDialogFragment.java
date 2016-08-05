package com.mudah.my.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import com.lib701.utils.ACUtils;
import com.mudah.my.R;
import com.mudah.my.activities.ChatActivity;
import com.mudah.my.utils.MudahUtil;

public class BlockChatRoomDialogFragment extends DialogFragment {

    private static final String KEY_HEADER = "HEADER";
    private static final String KEY_MSG = "MSG";
    private static final String KEY_TITLE_ACTION = "TITLE_ACTION";
    private static final String KEY_TYPE = "TYPE";
    private String title;
    private String message;
    private String titleActionButton;
    private String type;

    public static BlockChatRoomDialogFragment dialogInstance(String header, String message, String titleActionButton, String type) {

        Bundle args = new Bundle();

        args.putString(KEY_HEADER, header);
        args.putString(KEY_MSG, message);
        args.putString(KEY_TITLE_ACTION, titleActionButton);
        args.putString(KEY_TYPE, type);
        BlockChatRoomDialogFragment fragment = new BlockChatRoomDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.type = getArguments().getString(KEY_TYPE);
            this.title = getArguments().getString(KEY_HEADER);
            this.message = getArguments().getString(KEY_MSG);
            this.titleActionButton = getArguments().getString(KEY_TITLE_ACTION);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                .setTitle(title)
                .setMessage(ACUtils.getHtmlFromString(message))
                .setPositiveButton(titleActionButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ChatCafeFragment.OTHERS.equalsIgnoreCase(type)) {
                            dialog.dismiss();
                        } else {
                            StatusBlock status = new StatusBlock();
                            if (ChatCafeFragment.BLOCK.equalsIgnoreCase(type)) {
                                status.setType(ChatCafeFragment.BLOCK);
                                status.setIsStatusBlock(true);
                            } else {
                                status.setType(ChatCafeFragment.REPORT);
                            }
                            if (getActivity() instanceof ChatActivity) {
                                ChatActivity chatActivity = (ChatActivity) getActivity();
                                chatActivity.onCompleteBlockChat(status);
                            }
                            dialog.dismiss();
                        }
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

        if (!ChatCafeFragment.OTHERS.equalsIgnoreCase(type)) {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        AlertDialog dialog = builder.show();
        MudahUtil.hideDialogDivider(getContext(), dialog);

        return dialog;
    }

    public class StatusBlock {
        private String type;
        private boolean isStatusBlock;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isStatusBlock() {
            return isStatusBlock;
        }

        public void setIsStatusBlock(boolean isStatusBlock) {
            this.isStatusBlock = isStatusBlock;
        }
    }
}
