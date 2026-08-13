package com.zero.server.event;

import com.zero.Zero;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerTickEvent {

    public ServerTickEvent() {
        MinecraftForge.EVENT_BUS.register(new EntityDeathEvent());
    }

    @SubscribeEvent
    public void tickServer(TickEvent.ServerTickEvent event) {
        switch (event.phase) {
            case START:
                Zero.getPacketHandler().handleServerPackets();
                Zero.getPlayerHandler().ServerTick();
                break;
        }
    }

}
