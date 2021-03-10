package com.example.camguard;


import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class GAdminReceiver extends DeviceAdminReceiver {

    public void showToast(Context context, String str) {
        Toast.makeText(context, str,  Toast.LENGTH_SHORT  ).show();
    }

    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
}


