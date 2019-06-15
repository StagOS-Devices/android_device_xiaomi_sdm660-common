/*
 * Copyright (c) 2015 The CyanogenMod Project
 * Copyright (c) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import org.lineageos.settings.device.util.FileUtils;

import java.util.List;

import static org.lineageos.settings.device.DeviceSettings.SPECTRUM_SYSTEM_PROPERTY;

class AutoThermalConfig {
    private static final String TAG = "AutoThermalConfig";
    private static final boolean DEBUG = true;

    private static final Handler handler = new Handler();
    private ActivityRunnable activityRunnable;
    private boolean mRunning = false;

    private Context mContext;

    // Supported Thermal Modes
    private static final String MODE_BATTERY2 = "0";
    private static final String MODE_BATTERY = "1";
    private static final String MODE_BALANCE = "2";
    private static final String MODE_PERFORMANCE = "3";
    private static final String MODE_GAME = "4";

    private static String mDefaultMode = MODE_BALANCE;
    private static boolean mEnabled = false;

    AutoThermalConfig(Context context) {
        mContext = context;
        mEnabled = Settings.Secure.getInt(context.getContentResolver(),
                DeviceSettings.PREF_AUTO_THERMAL, 0) == 1;
        mDefaultMode = Settings.Secure.getString(context.getContentResolver(),
                DeviceSettings.PREF_SPECTRUM);
        if (mEnabled) {
            checkActivity(context);
        } else {
            sendThermalMessage(mDefaultMode);
        }
    }

    private void setThermalMode(String packagename) {
        switch (packagename) {
            // Benchmarks
            case "com.antutu.ABenchMark":
            case "com.antutu.benchmark.full":
            case "com.futuremark.dmandroid.application":
            case "com.primatelabs.geekbench":
                sendThermalMessage(MODE_PERFORMANCE);
                break;

            // Video
            case "com.google.android.youtube":
            case "com.netflix.mediaclient":
            case "com.google.android.videos":
            case "com.amazon.avod.thirdpartyclient":
            case "com.google.android.apps.youtube.kids":
            case "org.lineageos.jelly":
            case "com.android.chrome":
            case "com.UCMobile.intl":
            case "ch.deletescape.lawnchair.ci":
                sendThermalMessage(MODE_BATTERY);
                break;

            case "com.tencent.ig":
            case "skynet.cputhrottlingtest":
                sendThermalMessage(MODE_GAME);
                break;

            default:
                if (isGame(packagename) == 1) {
                    sendThermalMessage(MODE_GAME);
                } else {
                    sendThermalMessage(mDefaultMode);
                }
        }
    }

    private int isGame(String packageName) {
        int isGame = 0;
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            if (ai != null) {
                isGame = (ai.category == ApplicationInfo.CATEGORY_GAME ||
                        (ai.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME) ? 1 : 0;
            }
        } catch (PackageManager.NameNotFoundException ex) {
            // do nothing
        }
        return isGame;
    }

    private static void sendThermalMessage(String mode) {
        if (DEBUG) Log.d(TAG, "Change mode to: " + mode);
        FileUtils.setProp(SPECTRUM_SYSTEM_PROPERTY, mode);
    }

    void checkActivity(Context context) {
        activityRunnable = new ActivityRunnable(context);
        handler.postDelayed(activityRunnable, 500);
        mRunning = true;
    }

    void removeCallback() {
        if (DEBUG) Log.d(TAG, "Removed Callbacks");
        handler.removeCallbacks(activityRunnable);
        mRunning = false;
        sendThermalMessage(mDefaultMode);
    }

    private class ActivityRunnable implements Runnable {

        private Context context;

        private ActivityRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
            if (runningTasks != null && runningTasks.size() > 0) {
                ComponentName topActivity = runningTasks.get(0).topActivity;
                String foregroundApp = topActivity.getPackageName();
                handler.postDelayed(this, 500);
                setThermalMode(foregroundApp);
            }
        }

    }

    void update() {
        mEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                DeviceSettings.PREF_AUTO_THERMAL, 0) == 1;
        mDefaultMode = Settings.Secure.getString(mContext.getContentResolver(),
                DeviceSettings.PREF_SPECTRUM);
        if (mEnabled) {
            if (!mRunning) {
                checkActivity(mContext);
            }
        } else {
            if (mRunning) {
                removeCallback();
            }
            sendThermalMessage(mDefaultMode);
        }
    }
}
