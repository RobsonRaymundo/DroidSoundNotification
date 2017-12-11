package com.droid.ray.droidsoundnotification;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.droid.ray.droidsoundnotification.R.attr.icon;

@SuppressLint("NewApi")
public class DroidService extends AccessibilityService implements SensorEventListener, AudioManager.OnAudioFocusChangeListener {
    static int sdk_int = android.os.Build.VERSION.SDK_INT;

    private int timeNotification;
    public static boolean newNotification;
    public static boolean waitingTimeOutNofitication;

    @Override
    public void onServiceConnected() {

        super.onServiceConnected();

        if (sdk_int < 16)
        {
            AccessibilityServiceInfo localAccessibilityServiceInfo = new AccessibilityServiceInfo();
            localAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
            localAccessibilityServiceInfo.feedbackType = 16;
            localAccessibilityServiceInfo.notificationTimeout = 0L;
            setServiceInfo(localAccessibilityServiceInfo);
        }
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        newNotification = false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (waitingTimeOutNofitication == false) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String packageName = (String) event.getPackageName();
            if (mPrefs.getBoolean(packageName, false)) {
                Notification notification = (Notification) event.getParcelableData();
                CharSequence[] lines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                int i = 0;
                if (lines != null) {
                    for (CharSequence msg : lines) {
                        Log.d("Line " + i, (String) msg);
                        i += 1;
                    }
                }

                cancelAllNotification(getApplicationContext());


                postMessageInThread();
            }
        }
    }

    public static void cancelAllNotification(Context ctx) {
        try {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
            nMgr.cancelAll();
        }
        catch (Exception ex)
        {
            Log.d("DroidMessage", ex.getMessage());
        }
    }

    //implementation:
    private void postMessageInThread()  {
        Thread t = new Thread()  {
            @Override
            public void run() {
                try {
                    waitingTimeOutNofitication = true;
                    timeSleep(2000);
                    soundNotification(); // independente da configuracao, o som de notificação é emitido
                    if (DroidNotify.turnOnScreenChecked) {
                        turnOnScreen();
                    }
                    timeSleep(2000);
                } finally {
                    waitingTimeOutNofitication = false;
                }
            }
        };
        t.start();
    }

    @Override
    public void onInterrupt() {

    }

    private void timeSleep(int time)
    {
        try {
            Thread.sleep(time);
        }
        catch (Exception e) {}
    }

    @SuppressWarnings("deprecation")
        private void turnOnScreen() {

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        PowerManager.WakeLock wl = null;

        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "DroidNotification");

        int currentTimeOut = 0;
        String timeNotificationPref = mPrefs.getString("timeNotification", "10");
        timeNotification  = Integer.parseInt(timeNotificationPref) * 10;
        int timeOutScreen = timeNotification * 100;

        try
        {
            if (sdk_int >=17)
            {
                currentTimeOut = getTimeout();
                setTimeout(timeOutScreen, currentTimeOut);
            }

            wl.acquire();
        }
        finally
        {
            try
            {
                if (wl.isHeld())
                {
                    wl.release();
                }
                setTimeout(currentTimeOut, timeOutScreen);
            }
            catch (Exception ex){
            }
        }
    }

    private void soundNotification() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            r.setAudioAttributes(audioAttributes);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTimeout() {
        int screenTimeout = 0;
        try
        {
            screenTimeout = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        }
        catch (Exception ex)
        {
        }
        return screenTimeout;
    }

    private void setTimeout(int screenTimeOut, int currentTimeOut) {
        try
        {
            if (screenTimeOut > 0)
            {
                if (screenTimeOut != currentTimeOut)
                {
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.SCREEN_OFF_TIMEOUT, screenTimeOut);



                }
            }
        }
        catch (Exception ex)
        {
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }
}