package com.mudah.my.models;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.MalformedJsonException;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by ondo on 1/12/15.
 */
public class UserAccountModel {
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String TOKEN = "token";
    public static final String REGION = "region";
    public static final String SUBAREA = "subarea";
    public static final String USER_ID = "userId";
    public static final String FIRST_NAME = "firstname";
    public static final String LAST_NAME = "lastname";
    public static final String PHONE_NUMBER = "phone";
    public static final String GENDER = "gender";
    public static final String REGISTERED = "registered";
    public static final String AVATAR = "avatar";
    public static final String BIRTHDAY = "birthday";
    public static final String BIRTHYEAR = "birth_year";
    public static final String BIRTHMONTH = "birth_month";
    public static final String REGISTER_DATE = "register_date";
    public static final String USER_ACCOUNT_ID = "userAccountId";
    public static final String AVATAR_URL = "avatar_url";
    public static final String TOTAL_ADS = "totalAds";

    public static final String DEFAULT_CONFIG = "{" +
            " userId=''" +
            ", userAccountId=''" +
            ", username=''" +
            ", token=''" +
            ", email=''" +
            ", password=''" +
            ", registered=''" +
            ", totalAds=''" +
            ", imageUrl=''" +
            ", avatar=''" +
            ", phone=''" +
            ", region=''" +
            ", subarea=''" +
            ", firstname=''" +
            ", lastname=''" +
            ", birth_year=''" +
            ", birth_month=''" +
            ", register_date=''" +
            ", gender=''" +
            "}";

    // When users register, 1st creates a record at users table
    // then follow by user_accounts table. These two tables link by user_id column
    @SerializedName("userAccountId")
    private String userAccountId; //user_acct_id from user_accounts table
    @SerializedName("userId")
    private String userId; //member_id from users table (user_id)
    @SerializedName("token")
    private String token;
    @SerializedName("username")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("registered")
    private String registered = "0";
    @SerializedName("totalAds")
    private String totalAds = "0";
    @SerializedName("countryCode")
    private String countryCode;
    @SerializedName("lastSignIn")
    private long lastSignIn;
    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("firstname")
    private String firstname;
    @SerializedName("lastname")
    private String lastname;
    @SerializedName("avatar")
    private String avatar;
    @SerializedName("region")
    private String region;
    @SerializedName("subarea")
    private String subarea;
    @SerializedName("birthMonth")
    private String birthMonth;
    @SerializedName("birthYear")
    private String birthYear;
    @SerializedName("gender")
    private String gender;
    @SerializedName("register_date")
    private String register_date;
    @SerializedName("phone")
    private String phone;

    public UserAccountModel() {
    }

    public static UserAccountModel newInstance(Context context) {
        UserAccountModel userAccountData = new UserAccountModel();
        try {
            userAccountData = MudahUtil
                    .retrieveClassInSharedPreferences(
                            context,
                            MudahPreferencesUtils.USER_ACCOUNT_INFO,
                            UserAccountModel.class,
                            DEFAULT_CONFIG);
        } catch (MalformedJsonException e) {
            ACUtils.debug(e);
        } catch (JsonSyntaxException e) {
            ACUtils.debug(e);
        }
        return userAccountData;
    }

    public long getLastSignIn() {
        return lastSignIn;
    }

    public void setLastSignIn(long lastSignIn) {
        this.lastSignIn = lastSignIn;
    }

    public String getFullName() {
        String firstName = getFirstname();
        String lastName = getLastname();
        String fullName = Constants.EMPTY_STRING;

        if (!(ACUtils.isEmpty(firstName)))
            fullName += firstName;
        if (!(ACUtils.isEmpty(lastName))) {
            if (!(ACUtils.isEmpty(fullName))) {
                fullName += Constants.ONE_SPACE;
            }
            fullName += lastName;
        }

        return fullName;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSubarea() {
        return subarea;
    }

    public void setSubarea(String subarea) {
        this.subarea = subarea;
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getRegister_date() {
        return register_date;
    }

    public void setRegister_date(String register_date) {
        this.register_date = register_date;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(String userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String status) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTotalAds() {
        return totalAds;
    }

    public void setTotalAds(String total) {
        this.totalAds = total;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String status) {
        this.registered = status;
    }

    public void setUserDataPreferences(Context context, Map<String, Object> data) {
        Log.d();

        if (data.containsKey(USERNAME) && !ACUtils.isEmpty((String) data.get(USERNAME))) {
            username = (String) data.get(USERNAME);
            email = (String) data.get(USERNAME);
        }

        if (data.containsKey(EMAIL) && !ACUtils.isEmpty((String) data.get(EMAIL)))
            email = (String) data.get(EMAIL);

        if (data.containsKey(PASSWORD) && !ACUtils.isEmpty((String) data.get(PASSWORD)))
            password = (String) data.get(PASSWORD);

        if (data.containsKey(TOKEN) && !ACUtils.isEmpty((String) data.get(TOKEN))) {
            token = (String) data.get(TOKEN);
        }

        if (data.containsKey(USER_ID) && !ACUtils.isEmpty((String) data.get(USER_ID)))
            userId = (String) data.get(USER_ID);

        if (data.containsKey(USER_ACCOUNT_ID) && !ACUtils.isEmpty((String) data.get(USER_ACCOUNT_ID)))
            userAccountId = (String) data.get(USER_ACCOUNT_ID);

        if (data.containsKey(REGISTERED) && !ACUtils.isEmpty((String) data.get(REGISTERED)))
            registered = (String) data.get(REGISTERED);

        if (data.containsKey(TOTAL_ADS) && !ACUtils.isEmpty((String) data.get(TOTAL_ADS)))
            totalAds = (String) data.get(TOTAL_ADS);

        if (data.containsKey(AVATAR_URL) && !ACUtils.isEmpty((String) data.get(AVATAR_URL)))
            imageUrl = (String) data.get(AVATAR_URL);

        if (data.containsKey(AVATAR) && !ACUtils.isEmpty((String) data.get(AVATAR)))
            avatar = (String) data.get(AVATAR);

        if (data.containsKey(PHONE_NUMBER) && !ACUtils.isEmpty((String) data.get(PHONE_NUMBER)))
            phone = (String) data.get(PHONE_NUMBER);

        if (data.containsKey(REGION) && !ACUtils.isEmpty((String) data.get(REGION)))
            region = (String) data.get(REGION);

        if (data.containsKey(SUBAREA) && !ACUtils.isEmpty((String) data.get(SUBAREA)))
            subarea = (String) data.get(SUBAREA);

        if (data.containsKey(FIRST_NAME) && !ACUtils.isEmpty((String) data.get(FIRST_NAME)))
            firstname = (String) data.get(FIRST_NAME);

        if (data.containsKey(LAST_NAME) && !ACUtils.isEmpty((String) data.get(LAST_NAME)))
            lastname = (String) data.get(LAST_NAME);

        if (data.containsKey(BIRTHYEAR) && !ACUtils.isEmpty((String) data.get(BIRTHYEAR)))
            birthYear = (String) data.get(BIRTHYEAR);

        if (data.containsKey(BIRTHMONTH) && !ACUtils.isEmpty((String) data.get(BIRTHMONTH)))
            birthMonth = (String) data.get(BIRTHMONTH);

        if (data.containsKey(REGISTER_DATE) && !ACUtils.isEmpty((String) data.get(REGISTER_DATE)))
            register_date = (String) data.get(REGISTER_DATE);

        if (data.containsKey(GENDER) && !ACUtils.isEmpty((String) data.get(GENDER)))
            gender = (String) data.get(GENDER);

        saveUserDataPreferences(context);
    }

    public void setPasswordInPreferences(Context context, String passwordString) {
        if (!ACUtils.isEmpty(passwordString))
            password = passwordString;
        saveUserDataPreferences(context);
    }

    public void saveUserDataPreferences(Context context) {
        Log.d(toString());
        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.USER_ACCOUNT_INFO, this);
    }

    public boolean isLogin() {
        return !ACUtils.isEmpty(token);
    }

    public boolean isSessionValid() {
        Calendar now = Calendar.getInstance();
        Calendar lastSignInCalendar = Calendar.getInstance();
        lastSignInCalendar.setTimeInMillis(lastSignIn);
        lastSignInCalendar.add(Calendar.DATE, Config.validSignInDays);

        return now.compareTo(lastSignInCalendar) <= 0;
    }

    @Override
    public String toString() {

        return "{" +
                "userId=" + userId +
                ", userAccountId='" + userAccountId + '\'' +
                ", username='" + username + '\'' +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", registered='" + registered + '\'' +
                ", totalAds='" + totalAds + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", avatar='" + avatar + '\'' +
                ", phone='" + phone + '\'' +
                ", region='" + region + '\'' +
                ", subarea='" + subarea + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", birth_year='" + birthYear + '\'' +
                ", birth_month='" + birthMonth + '\'' +
                ", register_date='" + register_date + '\'' +
                ", gender='" + gender + '\'' +
                "}";
    }

    public void clearDataOnLogout(Context context) {
        Config.userAccount.setToken(Constants.EMPTY_STRING);
        Config.userAccount.setFirstname(Constants.EMPTY_STRING);
        Config.userAccount.setLastname(Constants.EMPTY_STRING);
        Config.userAccount.setRegion(Constants.EMPTY_STRING);
        Config.userAccount.setSubarea(Constants.EMPTY_STRING);
        Config.userAccount.setPhone(Constants.EMPTY_STRING);
        Config.userAccount.setGender(Constants.EMPTY_STRING);
        Config.userAccount.setBirthMonth(Constants.EMPTY_STRING);
        Config.userAccount.setBirthYear(Constants.EMPTY_STRING);
        Config.userAccount.setRegister_date(Constants.EMPTY_STRING);
        Config.userAccount.setAvatar(Constants.EMPTY_STRING);
        Config.userAccount.setImageUrl(Constants.EMPTY_STRING);
        Config.userAccount.saveUserDataPreferences(context);
    }
}
