package com.mudah.my.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.NothingSelectedSpinnerAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.fragments.DatePickerFragment;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.UserAccountModel;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalpana on 5/18/16.
 */
public class EditUserProfileActivity extends MudahBaseActivity {
    public static final int REQUEST_SUBREGION = 3;
    static final String EDIT_PROFILE = "Edit Profile";
    private static final String PHONE_PREFIX_0 = "0";
    private static final String PHONE_PREFIX_1 = "1";
    private static final int LOADER_USER_PROFILE = 0x02;
    private static final int REQUEST_PICTURE = 2;
    private static final int REQUEST_REGION = 1;
    private static final int REQUEST_PICTURE_PERMISSION = 4;
    private static final String PREFIX_MONTH = "0";
    private static final String SPACE = " ";
    public static boolean isUpdatedProfileRefreshMenu = false;
    private boolean isUpdatedProfilePicture = false;
    private Spinner gender;
    private EditText firstName;
    private EditText lastName;
    private TextView birthmonth;
    private TextView region;
    private TextView area;
    private EditText phone;
    private String imageUrl;
    private String imageFileName;
    private ImageView profilePic;
    private ProgressBar pbLoading;
    private ACReferences ref;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.region:
                    Intent intentRegion = new Intent(EditUserProfileActivity.this, ACAdsSearchRegionChooser.class);
                    intentRegion.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentRegion.putExtra(ACAdsSearchRegionChooser.HIDE_ALL_COUNTRY_OPTION, true);
                    startActivityForResult(intentRegion, REQUEST_REGION);
                    break;
                case R.id.area:
                    if (ACUtils.isEmpty(ref.getRegionId())) {
                        Toast.makeText(EditUserProfileActivity.this, R.string.location_first, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intentArea = new Intent(EditUserProfileActivity.this, ACAdsSearchMunicipalityChooser.class);
                        intentArea.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intentArea, REQUEST_SUBREGION);
                    }
                    break;
                case R.id.change_picture:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            (ContextCompat.checkSelfPermission(EditUserProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(EditUserProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PICTURE_PERMISSION);

                    } else {
                        Intent intentProfile = new Intent(EditUserProfileActivity.this, ChooseProfilePicture.class);
                        intentProfile.putExtra(ChooseProfilePicture.EXTRA_CHANGE, true);
                        intentProfile.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intentProfile, REQUEST_PICTURE);
                    }
                    break;
                case R.id.change_password:
                    Intent intentPassword = new Intent(EditUserProfileActivity.this, ChangePasswordActivity.class);
                    intentPassword.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentPassword);
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showConfirmDiscardProfile();
                return true;
            case R.id.save_profile:
                if (!clientValidationError()) {
                    getSupportLoaderManager().initLoader(LOADER_USER_PROFILE, null, asyncSaveUserProfile());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showConfirmDiscardProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfileActivity.this, R.style.MudahDialogStyle)
                .setMessage(R.string.discard_changes)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(EditUserProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        builder.show();
    }

    @Override
    public void onBackPressed() {
        showConfirmDiscardProfile();
    }

    private void setFocusToErrorField(EditText field) {
        ErrorHandler.tryToFocus(field);
    }

    private boolean clientValidationError() {
        String errorMessage;
        boolean errorExist = false;
        ErrorHandler.needsToRequestFocus = true;
        String phoneNumber = phone.getText().toString();

        if (!ACUtils.isEmpty(phoneNumber) && !phoneNumber.startsWith(PHONE_PREFIX_0) && !phoneNumber.startsWith(PHONE_PREFIX_1)) {
            setFocusToErrorField(phone);
            errorMessage = getResources().getString(R.string.contact_phone_invalid);
            ErrorHandler.setError(errorMessage, phone);
            errorExist = true;
        }

        if (!ACUtils.isEmpty(Config.userAccount.getFirstname()) && ACUtils.isEmpty(firstName.getText().toString())) {
            setFocusToErrorField(firstName);
            errorMessage = getResources().getString(R.string.contact_empty, getResources().getString(R.string.first_name));
            ErrorHandler.setError(errorMessage, firstName);
            errorExist = true;
        }

        if (!ACUtils.isEmpty(Config.userAccount.getLastname()) && ACUtils.isEmpty(lastName.getText().toString())) {
            setFocusToErrorField(lastName);
            errorMessage = getResources().getString(R.string.contact_empty, getResources().getString(R.string.last_name));
            ErrorHandler.setError(errorMessage, lastName);
            errorExist = true;
        }

        if (!ACUtils.isEmpty(Config.userAccount.getPhone()) && ACUtils.isEmpty(phoneNumber)) {
            setFocusToErrorField(phone);
            errorMessage = getResources().getString(R.string.contact_empty, getResources().getString(R.string.phone_number));
            ErrorHandler.setError(errorMessage, phone);
            errorExist = true;
        }

        return errorExist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();
        setContentView(R.layout.activity_edit_user_profile);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, EDIT_PROFILE);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);

        Button changePassword = (Button) findViewById(R.id.change_password);
        changePassword.setOnClickListener(onClickListener);

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        firstName = (EditText) findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);

        region = (TextView) findViewById(R.id.region);
        region.setOnClickListener(onClickListener);

        area = (TextView) findViewById(R.id.area);
        area.setOnClickListener(onClickListener);

        profilePic = (ImageView) findViewById(R.id.profile_picture);

        Button changePic = (Button) findViewById(R.id.change_picture);
        changePic.setOnClickListener(onClickListener);

        birthmonth = (TextView) findViewById(R.id.birthmonth);

        phone = (EditText) findViewById(R.id.phone_number);

        gender = (Spinner) findViewById(R.id.genderspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gender, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        gender.setAdapter(
                new NothingSelectedSpinnerAdapter(adapter,
                        R.layout.contact_spinner_row_nothing_selected,
                        getApplicationContext()));
        ref = ACReferences.getACReferences();
        updateProfile();
    }

    private void updateProfile() {
        UserAccountModel userAccountModel = Config.userAccount;
        String regionId = userAccountModel.getRegion();
        String subareaId = userAccountModel.getSubarea();

        if (!(ACUtils.isEmpty(userAccountModel.getFirstname()))) {
            firstName.setText(userAccountModel.getFirstname());
            firstName.setSelection(userAccountModel.getFirstname().length());
        }
        if (!(ACUtils.isEmpty(userAccountModel.getLastname())))
            lastName.setText(userAccountModel.getLastname());
        if (!(ACUtils.isEmpty(userAccountModel.getPhone())))
            phone.setText(userAccountModel.getPhone());

        region.setText(MudahUtil.getRegionName(regionId));
        area.setText(MudahUtil.getSubAreaName(subareaId));
        ref.setRegionId(regionId);
        ref.setMunicipalityId(subareaId);

        if ((Constants.FEMALE_SHORT).equalsIgnoreCase(userAccountModel.getGender())) {
            gender.setSelection(2);
        } else if ((Constants.MALE_SHORT).equalsIgnoreCase(userAccountModel.getGender())) {
            gender.setSelection(1);
        }
        if (!(ACUtils.isEmpty(userAccountModel.getBirthMonth())) && !(ACUtils.isEmpty(userAccountModel.getBirthYear()))) {
            birthmonth.setText(MudahUtil.convertDateToMonthAndYear(userAccountModel.getBirthYear() + "-" + userAccountModel.getBirthMonth() + "-01"));
        }
        if (!(ACUtils.isEmpty(userAccountModel.getAvatar()))) {
            imageFileName = userAccountModel.getAvatar();
            imageUrl = userAccountModel.getImageUrl();
            Picasso.with(getApplicationContext()).load(imageUrl).into(profilePic);
        }
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        DialogFragment datePickerFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        String birthday = birthmonth.getText().toString();
        if (!(ACUtils.isEmpty(birthday))) {
            String birthdaySplit[] = birthday.split(SPACE);
            if (birthdaySplit.length > 0) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new SimpleDateFormat("MMM").parse(birthdaySplit[0]));
                    int monthInt = cal.get(Calendar.MONTH);
                    bundle.putInt(Constants.MONTH, monthInt);
                    bundle.putString(Constants.YEAR, birthdaySplit[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        datePickerFragment.setArguments(bundle);
        datePickerFragment.show(getFragmentManager(), "Date Picker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private APILoader.Callbacks asyncSaveUserProfile() {
        Log.d();
        MudahUtil.hideSoftKeyboard(this);
        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
        String birthday = birthmonth.getText().toString();
        String year = Constants.EMPTY_STRING;
        String month = Constants.EMPTY_STRING;
        if (!(Constants.EMPTY_STRING).equalsIgnoreCase(birthday)) {
            year = birthday.split(Constants.ONE_SPACE)[1];
            month = birthday.split(Constants.ONE_SPACE)[0];
            int monthNumber = 0;
            try {
                monthNumber = format.parse(Constants.DUPLICATE_DATE + Constants.DATE_SEPERATOR + month + Constants.DATE_SEPERATOR + year).getMonth() + 1;
                month = Integer.toString(monthNumber);
                if (month.length() < 2)
                    month = PREFIX_MONTH + month;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        pbLoading.setVisibility(View.VISIBLE);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(UserAccountModel.FIRST_NAME, firstName.getText().toString());
        params.put(UserAccountModel.LAST_NAME, lastName.getText().toString());
        if (!ACUtils.isEmpty(ref.getRegionId())) {
            params.put(UserAccountModel.REGION, ref.getRegionId());
        } else {
            params.put(UserAccountModel.REGION, Constants.EMPTY_STRING);
        }
        if (!ACUtils.isEmpty(ref.getMunicipalityId())) {
            params.put(UserAccountModel.SUBAREA, ref.getMunicipalityId());
        } else {
            params.put(UserAccountModel.SUBAREA, Constants.EMPTY_STRING);
        }
        if (gender.getSelectedItem() != null) {
            params.put(UserAccountModel.GENDER, Character.toString(gender.getSelectedItem().toString().toLowerCase().charAt(0)));
        }
        if (!ACUtils.isEmpty(year)) {
            params.put(UserAccountModel.BIRTHYEAR, year);
        }
        if (!ACUtils.isEmpty(month)) {
            params.put(UserAccountModel.BIRTHMONTH, month);
        }
        params.put(Constants.USER_ACCOUNT_ID, Config.userAccount.getUserAccountId());

        if (!ACUtils.isEmpty(imageFileName)) {
            params.put(UserAccountModel.AVATAR, imageFileName);
            params.put(UserAccountModel.AVATAR_URL, imageUrl);
        }
        params.put(UserAccountModel.PHONE_NUMBER, phone.getText().toString());
        params.put(UserAccountModel.REGISTER_DATE, Config.userAccount.getRegister_date());

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "profile/" + Config.userAccount.getToken(), params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                String status = data.optString(Constants.STATUS);

                if (status.equals(Constants.OK)) {
                    if (isUpdatedProfilePicture) {
                        isUpdatedProfileRefreshMenu = true;
                    }
                    Config.userAccount.setUserDataPreferences(getApplicationContext(), params);
                    sendTag();
                    Toast.makeText(EditUserProfileActivity.this,
                            R.string.api_save_profile,
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditUserProfileActivity.this, UserProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    getSupportLoaderManager().destroyLoader(loader.getId());
                } else {
                    onLoadError(loader, data);
                }
                pbLoading.setVisibility(View.GONE);
            }

            private void sendTag() {
                KahunaHelper.tagEvent(KahunaHelper.LAST_SAVED_PROFILE);
                KahunaHelper.tagProfileAttributes(params);
                AmplitudeUtils.trackAllUserProperties();
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(EditUserProfileActivity.this,
                        R.string.api_save_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InsertAdActivity.isStartActivityForResult = false;//reset
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        ACReferences ref = ACReferences.getACReferences();
        if (requestCode == REQUEST_REGION) {
            String regionName = ACSettings.getACSettings().getRegionName(ref.getRegionId());
            region.setText(regionName);
            //clear sub-area
            area.setText(Constants.EMPTY_STRING);
            Config.userAccount.setSubarea(Constants.EMPTY_STRING);
            ref.setMunicipalityId(Constants.EMPTY_STRING);
        } else if (requestCode == REQUEST_SUBREGION) {
            String areaName = ACSettings.getACSettings().getMunicipalityName(ref.getMunicipalityId());
            area.setText(areaName);
        } else if (requestCode == REQUEST_PICTURE) {
            isUpdatedProfilePicture = true;
            imageUrl = data.getStringExtra(ChooseProfilePicture.RESULT_IMAGE_URL);
            imageFileName = data.getStringExtra(ChooseProfilePicture.RESULT_FILE_NAME);
            String status = data.getStringExtra(ChooseProfilePicture.RESULT_STATUS);
            if (status != null && status.equalsIgnoreCase(Constants.OK)) {
                if (imageUrl != null) {
                    // upload succeeded
                    ImageView imageView = (ImageView) findViewById(R.id.profile_picture);
                    Picasso.with(getApplicationContext()).load(imageUrl).into(imageView);
                }
            }
        }
    }
}
