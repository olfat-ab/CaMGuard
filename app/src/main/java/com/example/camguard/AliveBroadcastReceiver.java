package com.example.camguard;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public  class AliveBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("CaMGuardPrefs", context.MODE_PRIVATE );
        boolean m_isrestart = prefs.getBoolean("switch_protection",true );
        boolean m_islockmic = prefs.getBoolean("switch_lockmic",false );

        boolean isMiCPermissionOK =  intent.getBooleanExtra("isMiCPermissionOK" , false );

         if(m_isrestart) {
             Log.d("PService", "Service Restarted");
             Intent  m_srvintent = new Intent(context, prService.class);
             m_srvintent.putExtra("isMiCPermissionOK", isMiCPermissionOK);
             m_srvintent.putExtra("isMiC_locked", m_islockmic);

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 context.startForegroundService(m_srvintent);
             } else {
                 context.startService(m_srvintent);
             }
         }
         else
         {
             Intent  m_srvintent = new Intent(context, prService.class);
             context.stopService(m_srvintent);
         }

    }
}
