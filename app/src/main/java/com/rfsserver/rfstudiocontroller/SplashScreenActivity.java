package com.rfsserver.rfstudiocontroller;

import com.rfsserver.rfstudiocontroller.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashScreenActivity extends Activity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private SystemUiHider mSystemUiHider;

    private static final int LOGIN_DELAY = 3000;
    private static final String PREF_NAME = "settings";
    private HelperClass helperClass;
    private boolean connectivityState = false;
    private Activity myActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        helperClass = HelperClass.getInstance();
        helperClass.preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide(1500);
        fillProgress();
        checkConnectivity();
        if (helperClass.preferences.getBoolean("initialized", false)) {
            delayedLogin(LOGIN_DELAY);
        } else {
            Toast.makeText(SplashScreenActivity.this, "Please enter the details!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SplashScreenActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    Handler loginHandler = new Handler();
    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            login();
        }
    };

    private void fillProgress() {
        new Thread() {
            public void run() {
                final ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbSplashScreen);
                long curTime = SystemClock.uptimeMillis()+ (LOGIN_DELAY/progressBar.getMax());
                while(progressBar.getProgress()<progressBar.getMax()) {
                    if((curTime/30)==(SystemClock.uptimeMillis()/30)) {
                        progressBar.setProgress(progressBar.getProgress() + 1);
                        curTime = SystemClock.uptimeMillis()+ (LOGIN_DELAY/progressBar.getMax());
                    }
                }
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        }.start();
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void delayedLogin(int delayLogin) {
        loginHandler.removeCallbacks(loginRunnable);
        loginHandler.postDelayed(loginRunnable, delayLogin);
    }

    private void login() {
        if(connectivityState) {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void tryLogin(View view) {
        checkConnectivity();
        login();
    }

    private void checkConnectivity() {
        if(!helperClass.isOnline(this)) {
            Toast.makeText(SplashScreenActivity.this, "Please check the network connectivity", Toast.LENGTH_SHORT).show();
            return;
        }
        connectivityState = true;
    }
}
