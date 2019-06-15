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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import static org.lineageos.settings.device.DeviceSettings.PREF_AUTO_THERMAL;

public class AutoThermalConfigService extends Service {
    static final String TAG = "AutoThermalConfig";
    private static final boolean DEBUG = true;

    private SBinder mBinder = new SBinder();
    private Context mContext;
    private boolean mEnabled = false;
    private boolean mFlag;
    private AutoThermalConfig mAutoThermalConfig;

    class SBinder extends Binder {
        AutoThermalConfigService getService() {
            return AutoThermalConfigService.this;
        }
    }

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();
        mContext = this;
        mEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                PREF_AUTO_THERMAL, 0) == 1;
        mAutoThermalConfig = new AutoThermalConfig(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenStateReceiver, screenStateFilter);
        mFlag = false;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        mAutoThermalConfig.removeCallback();
        mContext.unregisterReceiver(mScreenStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void ScreenOn() {
        if (DEBUG) Log.d(TAG, "Screen on");
        if (mEnabled) {
            mAutoThermalConfig.checkActivity(mContext);
        }
    }

    private void ScreenOff() {
        if (DEBUG) Log.d(TAG, "Screen off");
        mFlag = true;
        if (mEnabled) {
            mAutoThermalConfig.removeCallback();
        }
    }

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mFlag) ScreenOn();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                ScreenOff();
            }
        }
    };

    public void update() {
        mEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                PREF_AUTO_THERMAL, 0) == 1;

        mAutoThermalConfig.update();
    }
}
