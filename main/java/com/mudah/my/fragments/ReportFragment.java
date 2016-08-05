package com.mudah.my.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.lib701.utils.IntentActionUtils;
import com.mudah.my.R;

public class ReportFragment extends Fragment {
    private String emailAddress;
    private String subject;
    private String body;

    public static ReportFragment instantiate(String emailAddress, String subject, String body) {
        ReportFragment fragment = new ReportFragment();
        fragment.emailAddress = emailAddress;
        fragment.subject = subject;
        fragment.body = body;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.report, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (result) return result;

        if (item.getItemId() == R.id.menu_report) {
            IntentActionUtils.email(emailAddress, subject, body, getActivity());
            return true;
        } else {
            return false;
        }
    }
}
