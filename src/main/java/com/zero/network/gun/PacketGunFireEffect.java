package com.zero.network.gun;

import com.zero.network.PacketBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketGunFireEffect extends PacketBase {

    public PacketGunFireEffect(){

    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {

    }
}
