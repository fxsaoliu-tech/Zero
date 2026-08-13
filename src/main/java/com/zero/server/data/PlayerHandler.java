package com.zero.server.data;

import com.zero.Zero;
import com.zero.server.raytracing.PlayerSnapshot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHandler {
    private static Map<UUID, PlayerData> players = new HashMap<>();

    public PlayerHandler() {
        Zero.eventRegister(this);
    }

    @SubscribeEvent
    public void onPlayerEvent(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        UUID uuid = player.getUniqueID();

        if (!players.containsKey(uuid)) {
            players.put(player.getUniqueID(), new PlayerData(player));
        }
    }

    @SubscribeEvent
    public void onPlayerEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityPlayer player = event.player;
        UUID uuid = player.getUniqueID();

        if (players.containsKey(uuid)) {
            players.remove(player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onPlayerEvent(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        UUID uuid = player.getUniqueID();

        if (players.containsKey(uuid)) {
            PlayerData data = players.get(uuid);
            data.reset();
        }
    }

    public void ServerTick() {
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            PlayerData data = entry.getValue();
            if (data != null) {
                data.onUpdate();
            }
        }
    }

    public static void ClientTick() {
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            PlayerData data = entry.getValue();
            if (data != null) {
                data.snapshots[0].renderSnapshot();
            }
        }
    }

    public static PlayerData getPlayerData(UUID uuid) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        }
        return null;
    }

}
