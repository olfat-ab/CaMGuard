package com.example.camguard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ActivityManager;
import android.app.Application;
import android.app.PendingIntent;

import android.content.ComponentName;
import android.content.Intent;


import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;


import androidx.core.app.NotificationCompat;

public class AppHelper extends Application {

    private SharedPreferences prefs;
    public boolean isMiCPermissionOK = false;



    public void onCreate() {
        super.onCreate();
        this.prefs = getSharedPreferences("CaMGuardPrefs", MODE_PRIVATE );

    }

    public boolean isMainServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    public void doStartService() {
        SavePreferencesBoolean("switch_protection", true);

        if (!isMainServiceRunning(prService.class)) {
            Intent intent =  new Intent(getApplicationContext(), AliveBroadcastReceiver.class);
            intent.putExtra("isMiCPermissionOK", isMiCPermissionOK);
            sendBroadcast(intent);
        }


   }

    public void doStopService() {
        SavePreferencesBoolean("switch_protection",false);
        Intent intent =  new Intent(getApplicationContext(), AliveBroadcastReceiver.class);
        sendBroadcast(intent);
    }

    public void callLockMic() {
        SavePreferencesBoolean("switch_lockmic",true);
    }

    public void callUnLockMic() {
        SavePreferencesBoolean("switch_lockmic",false);
    }
    ///////////////////////////////////////////////////////////////////

    public String loadPreferences(String str) {
        return this.prefs.getString(str, "");
    }

    public void SavePreferences(String str, String str2) {
        SharedPreferences.Editor edit = this.prefs.edit();
        edit.putString(str, str2);
        edit.commit();
    }

    public int loadPreferencesInt(String str) {
        return this.prefs.getInt(str, 0);
    }

    public void SavePreferencesInt(String str, int i) {
        SharedPreferences.Editor edit = this.prefs.edit();
        edit.putInt(str, i);
        edit.commit();
    }

    public long loadPreferencesLong(String str) {
        return this.prefs.getLong(str, 0);
    }

    public void SavePreferencesLong(String str, long j) {
        SharedPreferences.Editor edit = this.prefs.edit();
        edit.putLong(str, j);
        edit.commit();
    }

    public boolean loadPreferencesBoolean(String str) {
        return this.prefs.getBoolean(str, false);
    }

    public void SavePreferencesBoolean(String str, boolean z) {
        SharedPreferences.Editor edit = this.prefs.edit();
        edit.putBoolean(str, z);
        edit.commit();
    }

    public void DestroyPreferences() {
        this.prefs.edit().clear();
        this.prefs.edit().commit();
    }

///////////////////////////////////////////////////////////////////////////////////////////


}
