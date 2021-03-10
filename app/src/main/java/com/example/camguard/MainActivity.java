package com.example.camguard;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity   {
    AppHelper aHelper = null;
    DevicePolicyManager devicePolicyManager;
    ComponentName deviceComponent;
    public static final int ADMIN_REQUEST = 1;
    private final int _PERMISSIONS_RECORD_AUDIO = 1;
    Timer m_timer = new Timer();
    TimerTask mTimerTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getFragmentManager().beginTransaction()
//                .replace(R.id.pref_container, new PrefsFragment()).commit();

        this.aHelper = (AppHelper) getApplication();
        this.devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        this.deviceComponent = new ComponentName(this, GAdminReceiver.class);

        initializeCom();

        Switch msw_protection = findViewById(R.id.switch_protection);
        Switch msw_camerablock = findViewById(R.id.switch_camerablock);
        Switch msw_micblock = findViewById(R.id.switch_lockmic);


        msw_protection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    requestAudioPermissions();
                     aHelper.doStartService();
                    ((TextView) findViewById(R.id.textView_PStatus)).setText(R.string.pstatus_msg_start);

                }
                 else {
                    aHelper.doStopService();
                    ((TextView) findViewById(R.id.textView_PStatus)).setText(R.string.pstatus_msg_stop);
                    ((TextView) findViewById(R.id.textView_PStatus_onlycamera)).setVisibility(View.INVISIBLE);
                }
            }
        });

        msw_camerablock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if (!devicePolicyManager.isAdminActive(deviceComponent)) {
                        Intent intent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
                        intent.putExtra("android.app.extra.DEVICE_ADMIN", deviceComponent);
                        startActivityForResult(intent, ADMIN_REQUEST);
                    } else {
                        // this.appFunctions.doStopService();
                        devicePolicyManager.setCameraDisabled(deviceComponent, true);
                        aHelper.SavePreferencesBoolean("switch_camera_locked", true);
                        ((TextView) findViewById(R.id.textView_lockcamera)).setText(R.string.lckstatus_canera_disable);
                    }

                }
                else {
                    if (devicePolicyManager.isAdminActive(deviceComponent)) {
                        devicePolicyManager.setCameraDisabled(deviceComponent, false);
                        aHelper.SavePreferencesBoolean("switch_camera_locked", false);
                        ((TextView) findViewById(R.id.textView_lockcamera)).setText(R.string.lckstatus_canera_enable);
                    }
                }
            }
        });

        msw_micblock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    requestAudioPermissions();
                    if( aHelper.isMiCPermissionOK) {

                        aHelper.callLockMic();
                 }
                    else
                        ((Switch) findViewById(R.id.switch_lockmic)).setChecked(false);
                }
                else {

                    aHelper.callUnLockMic();

                }

                if (((Switch) findViewById(R.id.switch_protection)).isChecked()) {
                    ((Switch) findViewById(R.id.switch_protection)).setChecked(false);
                    //  doTimerTask();
                    Toast.makeText(MainActivity.this, "Please ReEnable Protection. ", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public void deviceAdminActivityResult() {
        if (this.devicePolicyManager.isAdminActive(this.deviceComponent)) {
            this.devicePolicyManager.setCameraDisabled(this.deviceComponent, true);
            return;
        }

        Toast.makeText(this, getResources().getString(R.string.permit_admin), Toast.LENGTH_LONG).show();
        ((Switch) findViewById(R.id.switch_camerablock)).setChecked(false);
        aHelper.SavePreferencesBoolean("switch_camera_locked", false);
        ((TextView) findViewById(R.id.textView_lockcamera)).setText(R.string.lckstatus_canera_enable);
    }



            private void initializeCom() {
                if (!aHelper.isMainServiceRunning(prService.class)) {
                    ((Switch) findViewById(R.id.switch_protection)).setChecked(false);
                    ((TextView) findViewById(R.id.textView_PStatus)).setText(R.string.pstatus_msg_stop);

                } else {
                    //  aHelper.clearNotification();
                    ((TextView) findViewById(R.id.textView_PStatus)).setText(R.string.pstatus_msg_start);
                    ((Switch) findViewById(R.id.switch_protection)).setChecked(true);
                }

                boolean lckst=aHelper.loadPreferencesBoolean("switch_camera_locked");
                ((Switch) findViewById(R.id.switch_camerablock)).setChecked(lckst);
                if(lckst)
                    ((TextView) findViewById(R.id.textView_lockcamera)).setText(R.string.lckstatus_canera_disable);
                else
                    ((TextView) findViewById(R.id.textView_lockcamera)).setText(R.string.lckstatus_canera_enable);

                if(aHelper.loadPreferencesBoolean("switch_lockmic"))
                    ((Switch) findViewById(R.id.switch_lockmic)).setChecked(true);
                else
                    ((Switch) findViewById(R.id.switch_lockmic)).setChecked(false);

            }
/*
    public void doTimerTask() {
        final Handler  m_handler = new Handler();
        mTimerTask = new TimerTask() {
            public void run() {
                m_handler.post(new Runnable() {
                    public void run() {
                        ((Switch) findViewById(R.id.switch_protection)).setChecked(true);
                    }
                });
            }
        };

        m_timer.schedule(mTimerTask, 1500, 1);  //
    }
*/

    @Override
    protected void onResume(){
        super.onResume();
         initializeCom();
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please Grant Permissions to Record Audio To Protect Microphone!", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, _PERMISSIONS_RECORD_AUDIO);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, _PERMISSIONS_RECORD_AUDIO);
            }
        }
        else
        {
            aHelper.isMiCPermissionOK = true;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADMIN_REQUEST:
                deviceAdminActivityResult();
                return;

            default:
                return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case _PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((TextView) findViewById(R.id.textView_PStatus_onlycamera)).setVisibility(View.INVISIBLE);
                    aHelper.isMiCPermissionOK = true;

                } else {

                    aHelper.isMiCPermissionOK = false;
                    ((TextView) findViewById(R.id.textView_PStatus_onlycamera)).setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Permissions On Microphone Denied , Mic Protection is Disabled!  Please Grant Audio Record Permission. ", Toast.LENGTH_LONG).show();
                }

                return;
            }
        }
    }

//
//    public static class PrefsFragment extends PreferenceFragment {
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            getPreferenceManager().setSharedPreferencesName("CaMGuardPrefs");
//            addPreferencesFromResource(R.xml.pref_main);
//        }
//    }

}
