package com.droid.ray.droidsoundnotification;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;

public class DroidNotify extends PreferenceActivity {

    private boolean active;
    private Preference service;
    public static boolean turnOnScreenChecked;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            addPreferencesFromResource(R.xml.preferences);

            service = (Preference) findPreference("service");
            service.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try
                    {
                        startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                    catch (Exception ex)
                    {}
                    return true;
                }
            });

            final CheckBoxPreference checkboxPrefSensorOn = (CheckBoxPreference) findPreference("turnOnScreen");
            checkboxPrefSensorOn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {


                    if (newValue.toString().equals("true"))
                    {
                        //SetSensorProximity(true);
                        turnOnScreenChecked = true;
                    }
                    else
                    {
                        //SetSensorProximity(false);
                        turnOnScreenChecked = false;
                    }

                    return true;
                }
            });

            turnOnScreenChecked = checkboxPrefSensorOn.isChecked();

        }
        catch (Exception ex)
        {  }
    }

    public void onResume() {
        super.onResume();
        active = isMyServiceRunning();
        if(active) {
            service.setTitle(R.string.app_active);
            service.setSummary(R.string.app_deactive);
        }
        else {
            service.setTitle(R.string.app_inactive);
            service.setSummary(R.string.app_activate);
        }
    }

    protected void onPause() {
        super.onPause();
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);


        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

                if (DroidService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
        }
        return false;
    }

    }
