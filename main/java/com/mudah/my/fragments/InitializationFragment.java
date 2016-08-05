package com.mudah.my.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InitializationFragment extends Fragment {
    public static final String TAG_VERSION_CHECK = "version_check";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new View(getActivity());
        view.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        VersionCheckFragment versionCheckFragment = new VersionCheckFragment();

        if (getFragmentManager().findFragmentByTag(TAG_VERSION_CHECK) == null) {
            getFragmentManager().beginTransaction()
                    .add(versionCheckFragment, TAG_VERSION_CHECK)
                    .commit();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}