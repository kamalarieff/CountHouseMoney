package com.mudah.my.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.squareup.picasso.Picasso;

public class LoadingFragment extends Fragment {
    private View vConnectionLost;
    private View vMaintenance;
    private ProgressBar pbLoading;
    private OnRetryListener onRetryListener;
    private boolean connectionLostShown;
    private boolean isShown = true;
    private TextView tvMainText;
    private TextView tvSubText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading, container, false);

        ImageView maintenanceImg = (ImageView) view.findViewById(R.id.imgv_maintenance);
        Picasso.with(view.getContext()).load(R.drawable.ic_maintenance).fit().centerInside().into(maintenanceImg);

        pbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
        vConnectionLost = view.findViewById(R.id.loading_connection_lost);
        ImageView connectionLostImg = (ImageView) view.findViewById(R.id.imgv_connection_lost);
        Picasso.with(view.getContext()).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);

        vConnectionLost.setVisibility(View.GONE);
        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConnectionLostShown(false);
                if (onRetryListener != null) {
                    onRetryListener.onRetry();
                }
            }
        });
        setConnectionLostShown(connectionLostShown, false);

        vMaintenance = view.findViewById(R.id.v_maintenance);
        vMaintenance.setVisibility(View.GONE);
        tvMainText = (TextView) view.findViewById(R.id.tv_maintenance_txt);
        tvSubText = (TextView) view.findViewById(R.id.tv_maintenance_sub_txt);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("activity= " + getActivity());
        if (getActivity() != null) {
            Log.d();
            ACUtils.unbindDrawables(getActivity().findViewById(R.id.RootView));
        }
        if (vConnectionLost != null) {
            ACUtils.unbindDrawables(vConnectionLost);
        }
        System.gc();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isShown == false && isVisible()) {
            hide();
        } else if (isShown == true && isHidden()) {
            show();
        }
    }

    public void setOnRetryListener(OnRetryListener onRetryListener) {
        this.onRetryListener = onRetryListener;
    }

    public void setConnectionLostShown(boolean show) {
        if (connectionLostShown == show) return;
        setConnectionLostShown(show, true);
    }

    private void setConnectionLostShown(boolean show, boolean animate) {
        Log.d("show=" + show + ", animate=" + animate);
        connectionLostShown = show;
        if (show == true) {
            if (animate && isVisible()) {
                pbLoading.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                vConnectionLost.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                pbLoading.clearAnimation();
                vConnectionLost.clearAnimation();
            }
            pbLoading.setVisibility(View.GONE);
            vConnectionLost.setVisibility(View.VISIBLE);
        } else {
            if (animate && isVisible()) {
                pbLoading.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                vConnectionLost.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                pbLoading.clearAnimation();
                vConnectionLost.clearAnimation();
            }
            pbLoading.setVisibility(View.VISIBLE);
            vConnectionLost.setVisibility(View.GONE);
        }
    }

    public void updateMaintenanceMode(boolean mode, boolean isInsertAd) {
        Log.d("enable: " + mode);
        if (getFragmentManager() != null && mode) {
            vMaintenance.setVisibility(View.VISIBLE);
            if (isInsertAd) {
                tvMainText.setText(ACUtils.getHtmlFromString(Config.maintenanceInsertAdText));
                tvSubText.setText(ACUtils.getHtmlFromString(Config.maintenanceInsertAdSubText));
            } else {
                tvMainText.setText(ACUtils.getHtmlFromString(Config.maintenanceListingText));
                tvSubText.setText(ACUtils.getHtmlFromString(Config.maintenanceListingSubText));
            }
            pbLoading.setVisibility(View.GONE);
            VersionCheckFragment.resetCheckStatus();

            getFragmentManager().beginTransaction()
                    .show(LoadingFragment.this)
                    .commitAllowingStateLoss();

        } else {
            vMaintenance.setVisibility(View.GONE);
        }
    }

    public void show() {
        show(false);
    }

    public void show(boolean connectionLostShown) {
        Log.d();
        isShown = true;
        setConnectionLostShown(connectionLostShown);
        if (getFragmentManager() != null) { // getFragmentManager() may be null when show() is called in AdsSearchFragment.onActivityCreated()?
            getFragmentManager().beginTransaction()
                    .show(LoadingFragment.this)
                    .commitAllowingStateLoss();
        }
    }

    public void hide() {
        Log.d();
        isShown = false;
        if (getFragmentManager() != null) { // getFragmentManager() may be null when hide() is called in AdsSearchFragment.onActivityCreated()?
            getFragmentManager().beginTransaction()
                    .hide(LoadingFragment.this)
                    .commitAllowingStateLoss();
        }
    }

    public interface OnRetryListener {
        void onRetry();
    }
}
