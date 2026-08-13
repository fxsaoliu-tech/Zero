package com.zero.server.data;

import com.zero.server.raytracing.PlayerSnapshot;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerData {
    private EntityPlayer player;

    private GunData gunData;

    public PlayerSnapshot[] snapshots;

    public PlayerData(EntityPlayer player) {
        this.player = player;
        snapshots = new PlayerSnapshot[8];
        reset();
    }

    public void onUpdate() {
        gunData.onUpdate();
        System.arraycopy(snapshots, 0, snapshots, 1, snapshots.length - 2 + 1);
        snapshots[0] = new PlayerSnapshot(player);
    }

    public void reset() {
        gunData = new GunData();
    }

    public GunData getGunData() {
        return gunData;
    }
}
