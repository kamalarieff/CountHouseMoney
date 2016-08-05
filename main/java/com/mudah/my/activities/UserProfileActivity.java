package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.models.UserAccountModel;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kalpana on 5/18/16.
 */
public class UserProfileActivity extends MudahBaseActivity {
    private static final String FEMALE = "Female";
    private static final String MALE = "Male";
    private TextView name;
    private TextView email;
    private TextView phone;
    private TextView location;
    private TextView gender;
    private TextView birthmonth;
    private TextView dateOfSignIn;
    private ImageView profileImg;
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_user_name:
                case R.id.phone_number:
                case R.id.location:
                case R.id.gender:
                case R.id.birthmonth:
                case R.id.iv_user_image:
                    Intent intentEditUserProfile = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
                    intentEditUserProfile.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentEditUserProfile);
                    break;
            }
        }
    };

    public static String getFormatedDate(String strDate, String currentFormat, String changeToFormat) {
        try {
            SimpleDateFormat df;
            df = new SimpleDateFormat(currentFormat);
            Date date = null;
            date = df.parse(strDate);
            df = new SimpleDateFormat(changeToFormat);
            return df.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Constants.EMPTY_STRING;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();
        setContentView(R.layout.activity_user_profile);
        getWindow().setBackgroundDrawable(null);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getResources().getString(R.string.your_profile));
        showActionBarRedBorder(true);

        name = (TextView) findViewById(R.id.tv_user_name);
        name.setOnClickListener(onClickListener);

        dateOfSignIn = (TextView) findViewById(R.id.tv_date);

        email = (TextView) findViewById(R.id.email);
        email.setText(Config.userAccount.getEmail());

        phone = (TextView) findViewById(R.id.phone_number);
        phone.setOnClickListener(onClickListener);

        location = (TextView) findViewById(R.id.location);
        location.setOnClickListener(onClickListener);

        gender = (TextView) findViewById(R.id.gender);
        gender.setOnClickListener(onClickListener);

        birthmonth = (TextView) findViewById(R.id.birthmonth);
        birthmonth.setOnClickListener(onClickListener);

        profileImg = (ImageView) findViewById(R.id.iv_user_image);
        profileImg.setOnClickListener(onClickListener);

        updateUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d();
        updateUserProfile();
    }

    private void updateUserProfile() {
        UserAccountModel userAccountModel = Config.userAccount;
        String region = userAccountModel.getRegion();
        String subarea = userAccountModel.getSubarea();

        String fullName = userAccountModel.getFullName();

        if (!(ACUtils.isEmpty(userAccountModel.getAvatar())))
            Picasso.with(getApplicationContext()).load(userAccountModel.getImageUrl()).into(profileImg);

        if (!(ACUtils.isEmpty(fullName))) {
            name.setText(fullName);
        }

        if (!(ACUtils.isEmpty(userAccountModel.getRegister_date()))) {
            String date = getFormatedDate(userAccountModel.getRegister_date(), "yyyy-MM-dd HH:mm:ss", "dd MMMM yyyy");
            dateOfSignIn.setText(getString(R.string.member_since, date));
        }

        if (!(ACUtils.isEmpty(userAccountModel.getPhone())))
            phone.setText(userAccountModel.getPhone());

        String locationString = Constants.EMPTY_STRING;
        String regionString = MudahUtil.getRegionName(region);
        String areaString = MudahUtil.getSubAreaName(subarea);
        if (!(Constants.EMPTY_STRING).equalsIgnoreCase(regionString))
            locationString += regionString;
        if (!(Constants.EMPTY_STRING).equalsIgnoreCase(areaString))
            locationString += Constants.COMMA + Constants.ONE_SPACE + areaString;

        if (!(ACUtils.isEmpty(locationString)))
            location.setText(locationString);

        if ((Constants.FEMALE_SHORT).equalsIgnoreCase(userAccountModel.getGender()))
            gender.setText(FEMALE);
        else if ((Constants.MALE_SHORT).equalsIgnoreCase(userAccountModel.getGender()))
            gender.setText(MALE);

        if (!(ACUtils.isEmpty(userAccountModel.getBirthMonth())) &&
                (!(ACUtils.isEmpty(userAccountModel.getBirthYear())))) {
            birthmonth.setText(MudahUtil.convertDateToMonthAndYear(userAccountModel.getBirthYear() + Constants.DATE_SEPERATOR + userAccountModel.getBirthMonth() + Constants.DATE_SEPERATOR + Constants.DUPLICATE_DATE));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                Intent intentEditUserProfile = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
                intentEditUserProfile.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentEditUserProfile);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
