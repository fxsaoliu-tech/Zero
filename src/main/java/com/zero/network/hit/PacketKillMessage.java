package com.zero.network.hit;

import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.network.PacketBase;
import com.zero.server.type.GunType;
import com.zero.server.type.InfoType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketKillMessage extends PacketBase {
    private boolean head;
    private InfoType info;
    private String beKilled;
    private String killer;

    public PacketKillMessage() {

    }

    public PacketKillMessage(boolean head, InfoType info, String beKilled, String killer) {
        this.head = head;
        this.info = info;
        this.beKilled = beKilled;
        this.killer = killer;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeBoolean(this.head);
        writeUTF(data, this.info.id);
        writeUTF(data, this.beKilled);
        writeUTF(data, this.killer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.head = data.readBoolean();
        this.info = InfoType.getInfoType(readUTF(data));
        this.beKilled = readUTF(data);
        this.killer = readUTF(data);
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (info instanceof GunType) {
            GunType gunType = (GunType) info;
            GunSoundPlayManager.playKillSound(player, gunType);
        }
    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {

    }
}
