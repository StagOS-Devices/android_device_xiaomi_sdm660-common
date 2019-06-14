package org.lineageos.settings.device;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class PerformanceTileService extends TileService {

    @Override
    public void onStartListening() {
        int currentState = FileUtils.getProp(DeviceSettings.SPECTRUM_SYSTEM_PROPERTY, 2);

        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel(getResources().getStringArray(R.array.spectrum_profiles)[currentState]);

        tile.updateTile();
        super.onStartListening();
    }

    @Override
    public void onClick() {
        int currentState = FileUtils.getProp(DeviceSettings.SPECTRUM_SYSTEM_PROPERTY, 2);

        int nextState;
        if (currentState == 4) {
            nextState = 0;
        } else {
            nextState = currentState + 1;
        }

        Tile tile = getQsTile();
        FileUtils.setProp(DeviceSettings.SPECTRUM_SYSTEM_PROPERTY, nextState);
        tile.setLabel(getResources().getStringArray(R.array.spectrum_profiles)[nextState]);

        tile.updateTile();
        super.onClick();
    }
}
