package com.example.camguard;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



public class prService extends Service  {

    private CameraManager mCameraManager;
    private Object mCameraCallback;
    private NotificationCompat.Builder NotificationBuilderAlerts = null ;
    private NotificationManager NotificationManagerAlerts = null;
    public AudioRecord mRecorder;
    private final IBinder binder = new SBinder();


    private boolean isMiCPermissionOK = false;
    private boolean isMiCLockedOK = false;
    private boolean KeepChecking = false;

    private final int m_notify_id = 2590;
    private final int m_notify_id2 = 2591;
    private final int m_notify_id3 = 2592;
    private boolean m_camera_notify_flag = false;
    private int m_camera_notify_type = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            if (this.NotificationManagerAlerts == null) {
                this.NotificationManagerAlerts = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            } else {
                this.NotificationManagerAlerts.cancel(m_notify_id);
            }

            if(NotificationBuilderAlerts == null)
                NotificationBuilderAlerts = new NotificationCompat.Builder(this, "C0231");

            this.NotificationBuilderAlerts.setSmallIcon(R.drawable.shieldicon2).setCategory(NotificationCompat.CATEGORY_SERVICE)
                     .setContentTitle("CaMGuard").setWhen(System.currentTimeMillis())
                     .setAutoCancel(true).setOngoing(true).setContentText("Protection is Enabled!");

                startForeground(1290, this.NotificationBuilderAlerts.build());
                clearNotification(m_notify_id);


        } else {

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            mCameraCallback = new CameraManager.AvailabilityCallback() {
                @Override
                public void onCameraAvailable(String cameraId) {
                    super.onCameraAvailable(cameraId);
                    m_camera_notify_flag = false;
                //    UpdateWidgets("camera is safe ", 0);

                }

                @Override
                public void onCameraUnavailable(String cameraId) {
                    super.onCameraUnavailable(cameraId);

                    try {
                        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);

                        if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                            m_camera_notify_type = 0;
                            m_camera_notify_flag = true;
                            startNotifyCamera();

                       //     UpdateWidgets("camera is busy - front", 1);
                        } else {
                            m_camera_notify_type = 1;
                            m_camera_notify_flag = true;
                            startNotifyCamera();
                        //    UpdateWidgets("camera is busy - rear ", 1);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            mCameraManager.registerAvailabilityCallback(
                    (CameraManager.AvailabilityCallback) mCameraCallback, null);
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("PService", "Service Started");
        if (intent != null) {
            isMiCPermissionOK =  intent.getBooleanExtra("isMiCPermissionOK" , false );
            isMiCLockedOK =  intent.getBooleanExtra("isMiC_locked" , false );
        }

        if(isMiCPermissionOK) {

            if(isMiCLockedOK) {
                goLockMic();
            }
            else {
                KeepChecking = true;
                startMicChecker();
            }
        }

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        KeepChecking = false;
        Log.d("PService", "Service Stoped");
        goUnLockMic();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCameraManager.unregisterAvailabilityCallback((CameraManager.AvailabilityCallback) mCameraCallback);
        }

        Intent intent =  new Intent(getApplicationContext(), AliveBroadcastReceiver.class);
        intent.putExtra("isMiCPermissionOK", isMiCPermissionOK);
        sendBroadcast(intent);
     }

    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCameraApp(Context context, CharSequence packageName) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        List<ResolveInfo> imageCaptureActivites = context.getPackageManager().queryIntentActivities(intent, 0);
        // Log.d("CameraApp","Camera app :" + name.toString());
        for (ResolveInfo info : imageCaptureActivites) {
            if (info.activityInfo.packageName.contains(packageName.toString())) {
                return true;
            }
        }
        return false;
    }


    public void showNotification(String title, String msgbody, int mnotifyid) {

        if (this.NotificationManagerAlerts == null) {
            this.NotificationManagerAlerts = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        } else {

            //this.NotificationManagerAlerts.cancel(mnotifyid);
        }

        if(NotificationBuilderAlerts == null)
            NotificationBuilderAlerts = new NotificationCompat.Builder(this, "C0231");

        this.NotificationBuilderAlerts.setSmallIcon(R.drawable.warning).setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(title).setWhen(System.currentTimeMillis())
                .setAutoCancel(false).setOngoing(false).setContentText(msgbody);

        if (this.NotificationManagerAlerts != null) {
            this.NotificationManagerAlerts.notify(mnotifyid, this.NotificationBuilderAlerts.build());
        }
    }

    public void clearNotification(int id) {

        if (this.NotificationManagerAlerts == null) {
            this.NotificationManagerAlerts = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        } else {
            this.NotificationManagerAlerts.cancel(id);
        }

    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("C0231", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getCurrentTime()
    {
        Date d=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
        return  sdf.format(d);
    }


    private void startMicChecker() {
        final boolean[] isfree = {true};
        new Thread(new Runnable() {
            public void run() {
                do {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(KeepChecking)
                      isfree[0] = isMicAvailable();
                 //   Log.d("PService", "mic is free :" + String.valueOf(isfree));

                    if(!isfree[0] && KeepChecking)
                    {

                        showNotification("Alert!","someone using microphone! - at : "+
                                getCurrentTime(), m_notify_id3);

                    }


                } while (KeepChecking);
            }
        }).start();

    }


    private void startNotifyCamera() {
        new Thread(new Runnable() {
            public void run() {
                while (m_camera_notify_flag)  {
                    if(m_camera_notify_type == 0)
                    {
                               showNotification("Alert!","someone using camera! - front at : "+
                                getCurrentTime(), m_notify_id2 );
                    }
                    else if (m_camera_notify_type == 1)
                    {

                        showNotification("Alert!","someone using camera! - rear at : "+
                                getCurrentTime(), m_notify_id2);
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }



    public boolean  isMicAvailable() {
        try {
            if (mRecorder != null) {
                this.mRecorder.stop();
                this.mRecorder.release();
                this.mRecorder = null;
            }
        } catch (Exception ex) {
        }
        if (this.mRecorder == null) {
            this.mRecorder = findAudioRecord();
        }

        if (this.mRecorder == null) {
            return true;
        }

        try {
            this.mRecorder.startRecording();
            if (this.mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                this.mRecorder.stop();
                this.mRecorder.release();
                this.mRecorder = null;
               return true;
            }

        } catch (Exception ex) {
            this.mRecorder.release();
            this.mRecorder = null;
        }

        return false;
    }



    public AudioRecord findAudioRecord() {

        int[] RECORDER_SAMPLERATE = {8000, 11025, 16000, 22050, 44100};
        int[] RECORDER_CHANNELS = {16, 12};
        int m_encoding = AudioFormat.ENCODING_PCM_16BIT;

        for(int i=0; i<RECORDER_SAMPLERATE.length ; i++) {
            for(int j=0; j<RECORDER_CHANNELS.length ; j++) {
                    try {
                        int minBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE[i], RECORDER_CHANNELS[j], m_encoding);
                        if (minBufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            try {
                                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE[i] ,
                                        RECORDER_CHANNELS[j] , m_encoding , minBufferSize);
                                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                                    return audioRecord;
                                }
                            } catch (Exception e) {

                               // Log.e("PService", "findRecord", e);

                            }
                        }
                    } catch (Exception e2) {
                        //Log.e("PService", " findRecord", e2);
                    }
                }
        }
        return null;
    }

    public void goLockMic() {
        Log.d("PService", "mic  locked" );
           boolean ismicinuse = true;
            try {
                this.mRecorder.stop();
                this.mRecorder.release();
                this.mRecorder = null;
            } catch (Exception ex) {
            }
            if (this.mRecorder == null) {
                this.mRecorder = findAudioRecord();
            }
            try {
                this.mRecorder.startRecording();
                if (this.mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    ismicinuse = false;

                }
            } catch (Exception ex) {
            }

            if (ismicinuse) {
                try {
                    this.mRecorder.release();
                    this.mRecorder = null;
                } catch (Exception ex) {
                }

                Toast.makeText(this, "Currently Microphone is using! Please Stop using Mic and try again", Toast.LENGTH_LONG).show();
            }

    }

    public void goUnLockMic() {
        try {
            Log.d("PService", "mic unlocked" );
            if (mRecorder != null) {
                this.mRecorder.stop();
                this.mRecorder.release();
                this.mRecorder = null;
            }
        } catch (Exception ex) {
        }
    }

    public class SBinder extends Binder {
        prService getInstance() {
            return prService.this;
        }
    }



/*
    private void UpdateWidgets(String msg_disply, int status) {
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.privacy_app_widget);
        view.setTextViewText(R.id.appwidget_msg, msg_disply);
        if (status == 1) {
            //view.setTextColor(R.id.appwidget_msg, getColor(R.color.Red));
            view.setInt(R.id.wg_container, "setBackgroundColor", getColor(R.color.Red));
        } else {
            //view.setTextColor(R.id.appwidget_msg, getColor(R.color.Green));
            view.setInt(R.id.wg_container, "setBackgroundColor", getColor(R.color.Green));
        }
        ComponentName theWidget = new ComponentName(this, PrivacyAppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(theWidget, view);
    }
*/
}

