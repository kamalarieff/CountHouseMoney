package com.mudah.my.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lib701.utils.PreferencesUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;

public class NotificationsFragment extends Fragment {
    private Button bUpgradeNo;
    private Button bUpgradeYes;
    private Button bUpgradeNever;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications, container, false);

        bUpgradeNever = (Button) view.findViewById(R.id.ib_update_never);
        bUpgradeNever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesUtils.getSharedPreferences(getActivity()).edit().putInt("never_ask_for_upgrade", 1).apply();
                Config.UPGRADE_PREFERENCES = 1;
                hide();
            }
        });

        bUpgradeNo = (Button) view.findViewById(R.id.ib_update_no);
        bUpgradeNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationsFragment.this.hide();
                Config.upgrade = false;
            }
        });

        bUpgradeYes = (Button) view.findViewById(R.id.ib_update_yes);
        bUpgradeYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setCancelable(true)
                        .setTitle(R.string.go_to_google_play)
                        .setMessage(R.string.go_to_google_play_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id="
                                        + getActivity().getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_alert_failure_button_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                            }
                        })
                        .create();
                dialog.show();
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void show() {
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .show(NotificationsFragment.this).commitAllowingStateLoss();
        }
    }

    public void hide() {
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .hide(NotificationsFragment.this).commitAllowingStateLoss();
        }
    }
}
