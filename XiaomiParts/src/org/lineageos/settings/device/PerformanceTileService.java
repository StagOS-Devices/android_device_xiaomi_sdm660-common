package org.lineageos.settings.device;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import static org.lineageos.settings.device.DeviceSettings.PREF_SPECTRUM;

public class PerformanceTileService extends TileService {

    private boolean mBound = false;
    private AutoThermalConfigService mAutoThermalConfigService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            AutoThermalConfigService.SBinder binder = (AutoThermalConfigService.SBinder) service;
            mAutoThermalConfigService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onStartListening() {
        int currentState = Integer.parseInt(Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), PREF_SPECTRUM));

        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel(getResources().getStringArray(R.array.spectrum_profiles)[currentState]);
        Intent intent = new Intent(getApplicationContext(), AutoThermalConfigService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        tile.updateTile();
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        getApplicationContext().unbindService(mConnection);
        mBound = false;
        super.onStopListening();
    }

    @Override
    public void onClick() {
        int currentState = Integer.parseInt(Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), PREF_SPECTRUM));

        int nextState;
        if (currentState == 4) {
            nextState = 0;
        } else {
            nextState = currentState + 1;
        }

        Tile tile = getQsTile();
        Settings.Secure.putString(getApplicationContext().getContentResolver(),
                PREF_SPECTRUM, String.valueOf(nextState));
        if (mBound) {
            mAutoThermalConfigService.update();
        }
        tile.setLabel(getResources().getStringArray(R.array.spectrum_profiles)[nextState]);

        tile.updateTile();
        super.onClick();
    }
}
