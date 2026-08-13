package com.zero.network.gun;

import com.zero.client.gui.gun.GunAttachmentScreen;
import com.zero.network.PacketBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketGunRefreshRefitScreen extends PacketBase {

    public PacketGunRefreshRefitScreen() {

    }


    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (Minecraft.getMinecraft().currentScreen instanceof GunAttachmentScreen) {
            Minecraft.getMinecraft().currentScreen.initGui();
        }
    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {

    }
}
