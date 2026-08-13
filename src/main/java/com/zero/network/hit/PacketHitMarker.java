package com.zero.network.hit;

import com.zero.client.event.ClientTickEvent;
import com.zero.network.PacketBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketHitMarker extends PacketBase {

    public PacketHitMarker() {
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        ClientTickEvent.hitTime = 20;
    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {

    }
}
