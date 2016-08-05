package com.mudah.my.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.comscore.analytics.comScore;
import com.crashlytics.android.Crashlytics;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.fragments.InitializationFragment;
import com.mudah.my.fragments.VersionCheckFragment;
import com.mudah.my.services.PullService;
import com.mudah.my.services.RegistrationIntentService;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.AppsFlyerUtils;
import com.mudah.my.utils.MudahUtil;
import com.optimizely.Optimizely;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

/**
 * Splash screen activity
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private static final String USER_IP = "UserIP";
    // used to know if the back button was pressed in the splash screen activity and avoid opening the next activity
    private boolean mIsBackButtonPressed;
    private ImageView splashImage;
    private BroadcastReceiver loadConfigBroadcastReceiver;
    private ProgressBar loadConfigProgressBar;
    private Calendar startLoadingTime;
    private VersionCheckFragment versionCheckFragment;
    private String chatRoomId;
    private int windowWidth;
    private int windowHeight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        startLoadingTime = Calendar.getInstance();
        splashImage = (ImageView) findViewById(R.id.splash_image);
        splashImage.setBackgroundColor(Color.parseColor(Config.splashBgColor));
        //load splash image
        getWindowSize();
        loadSplashImage();
        //init all the third party libraries
        initLibraries();
        loadConfigProgressBar = (ProgressBar) findViewById(R.id.load_config_progress_bar);
        loadConfigBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d();
                loadConfigProgressBar.setVisibility(ProgressBar.GONE);
                initAmplitude();
                initOptimizely();
                //load new regions and categories with services
                loadDataFromAPI();

                Calendar now = Calendar.getInstance();
                long timeDiff = now.getTimeInMillis() - startLoadingTime.getTimeInMillis();
                //if the load time less than SPLASH_DURATION, show splash screen for 1 second before redirecting users
                if (timeDiff < SPLASH_DURATION) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            redirectToMainPage();
                        }
                    }, 1000);
                } else {
                    redirectToMainPage();
                }

            }
        };

        versionCheckFragment = new VersionCheckFragment();
        versionCheckFragment.setWaitUntilFinish(true);
        versionCheckFragment.setWindowScreenSize(windowWidth, windowHeight);

        //Remove inactive user notification
        MudahUtil.clearNotificationsByID(getBaseContext(), Config.NOTIFICATION_INACTIVE_USER_ID);

        if (getIntent() != null) {
            chatRoomId = getIntent().getStringExtra(Constants.PUSH_CHAT_ROOM_ID);
        }

    }

    private void initAmplitude() {
        AmplitudeUtils.InitializeAmplitude(SplashScreenActivity.this, getApplication());
        if (Config.userAccount.isLogin()) {
            AmplitudeUtils.trackAllUserProperties();
        }
        AmplitudeUtils.tagEvent(AmplitudeUtils.START_APP);
    }

    private void initOptimizely() {
        if (Config.enableOptimizely) {
            Optimizely.setVerboseLogging(Log.isDebug);
            //To programmatically enable optimizely in editor mode
//            if (Log.isDebug)
//                Optimizely.enableEditor();
            Optimizely.startOptimizelyWithAPIToken(Config.OPTIMIZELY_KEY, getApplication());
        }
    }

    private void initLibraries() {
        try {
            //Send Tracking to AppsFlyer (detect installations, sessions, and updates.)
            AppsFlyerUtils.init(getApplication());
            gcmRegister();
        } catch (Exception ignore) {
            ACUtils.debug(ignore);
        }

    }

    private void redirectToMainPage() {
        Intent intent;
        if (!mIsBackButtonPressed) {
            Crashlytics.getInstance().core.setString(USER_IP, Config.userIP);
            // start the home screen if the back button wasn't pressed already
            if (!ACUtils.isEmpty(chatRoomId)) {
                //if user has not logged in - > Go to Sign In page
                //If user has logged in, go to chat page
                if (!Config.userAccount.isLogin()) {
                    intent = new Intent(SplashScreenActivity.this, SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Constants.PUSH_CHAT_ROOM_ID, chatRoomId);
                } else {
                    intent = null;
                    ChatActivity.buildStackChatRoomListActivity(SplashScreenActivity.this, chatRoomId);
                }
                Log.d("redirect to roomId: " + chatRoomId);
            } else {
                intent = new Intent(SplashScreenActivity.this, HomepageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            if (intent != null) {
                startActivity(intent);
            }
        }
        // make sure we close the splash screen so the user won't come back when it presses back key
        finish();
    }

    private void loadDataFromAPI() {
        Intent mServiceIntent = new Intent(this, PullService.class);
        startService(mServiceIntent);
    }

    private void gcmRegister() {
        if (MudahUtil.checkPlayServices(this)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loadConfigBroadcastReceiver);
        // To avoid OutOfMemory, removes the reference to the activity, so that it can be garbage collected.
        // Ref: http://stackoverflow.com/questions/9536521/outofmemoryerror-when-loading-activities
        if (splashImage != null && splashImage.getBackground() != null) {
            splashImage.getBackground().setCallback(null);
            splashImage = null;
        }
        System.gc();
    }

    private void loadSplashImage() {
        Log.d(Config.splashUrl + " size: " + windowWidth + " x " + windowHeight);
        if (ACUtils.isEmpty(Config.splashUrl)) {
            //no need to store the default image in memory
            //because next time, we will load from the splash url
            Picasso.with(getApplicationContext())
                    .load(R.drawable.splash)
                    .resize(windowWidth, windowHeight)
                    .centerCrop()
                    .onlyScaleDown()
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(splashImage);
        } else {
            Picasso.with(getApplicationContext())
                    .load(Config.splashUrl)
                    .error(R.drawable.splash)
                    .resize(windowWidth, windowHeight)
                    .centerCrop()
                    .onlyScaleDown()
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(splashImage);
        }
    }

    private void getWindowSize() {
        Point point = MudahUtil.getWindowSize(this);
        if (point != null) {
            windowHeight = point.y;
            windowWidth = point.x;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade, R.anim.hold);
    }

    @Override
    public void onBackPressed() {
        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();
        // Notify comScore about lifecycle usage
        comScore.onEnterForeground();
        LocalBroadcastManager.getInstance(this).registerReceiver(loadConfigBroadcastReceiver,
                new IntentFilter(PreferencesUtils.LOAD_CONFIG_COMPLETE));

        if (versionCheckFragment != null && getSupportFragmentManager().findFragmentByTag(InitializationFragment.TAG_VERSION_CHECK) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(versionCheckFragment, InitializationFragment.TAG_VERSION_CHECK)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Notify comScore about lifecycle usage
        comScore.onExitForeground();
    }

}