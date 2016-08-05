package com.mudah.my.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.mudah.my.R;
import com.mudah.my.configs.Constants;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by kalpana on 5/24/16.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the current date as the default date in the date picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (getArguments().get(Constants.MONTH) != null) {
            month = Integer.parseInt(getArguments().get(Constants.MONTH).toString());
        }
        if (getArguments().getString(Constants.YEAR) != null) {
            year = Integer.parseInt(getArguments().getString(Constants.YEAR));
        }
        /*
            Get Android DatePickerDialog without day
            - This code does not work
            THEME_DEVICE_DEFAULT_DARK
            THEME_DEVICE_DEFAULT_LIGHT
            - This code work fine
            THEME_HOLO_DARK
            THEME_HOLO_LIGHT
            THEME_TRADITIONAL
         */

        final DatePickerDialog dpd = new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, this, year, month, day) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                int day = getContext().getResources().getIdentifier("android:id/day", null, null);
                if (day != 0) {
                    View dayPicker = findViewById(day);
                    if (dayPicker != null) {
                        //Set Day view visibility Off/Gone
                        dayPicker.setVisibility(View.GONE);
                    }
                }
            }
        };
        dpd.getDatePicker().setMaxDate(new Date().getTime());
        dpd.setTitle(Constants.EMPTY_STRING);
        return dpd;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        //Do something with the date chosen by the user
        TextView birthday = (TextView) getActivity().findViewById(R.id.birthmonth);
        birthday.setText(new StringBuilder().append(getMonth(month)).append(" ").append(year));
    }

    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
}
