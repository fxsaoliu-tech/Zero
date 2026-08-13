package com.zero.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class PacketBase {

    public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf data);

    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf data);

    @SideOnly(Side.CLIENT)
    public abstract void handleClientSide(EntityPlayer player);

    public abstract void handleServerSide(EntityPlayerMP player);

    public static void writeUTF(ByteBuf data, String s) {
        ByteBufUtils.writeUTF8String(data, s);
    }

    public static String readUTF(ByteBuf data) {
        return ByteBufUtils.readUTF8String(data);
    }
}
