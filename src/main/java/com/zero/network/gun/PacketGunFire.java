package com.zero.network.gun;

import com.zero.Zero;
import com.zero.network.PacketBase;
import com.zero.server.data.PlayerData;
import com.zero.server.data.PlayerHandler;
import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class PacketGunFire extends PacketBase {
    private int interval;
    private float aim;
    private GunFireType type;

    public PacketGunFire() {

    }

    public PacketGunFire(int interval, float aim, GunFireType fireType) {
        this.interval = interval;
        this.aim = aim;
        this.type = fireType;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(interval);
        data.writeFloat(aim);
        data.writeByte(type.ordinal());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        interval = data.readInt();
        aim = data.readFloat();
        type = GunFireType.getFireType(data.readByte());
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemGun) {
            ItemGun gun = (ItemGun) stack.getItem();
            if (gun.getCurrentAmmo(stack) > 0 && interval == gun.getType().shootRpm && type == gun.getType().getFireType(stack)) {
                PlayerData playerData = PlayerHandler.getPlayerData(player.getUniqueID());
                if (playerData != null) {
                    gun.shootServer(player, stack, aim, player.world, playerData.getGunData());
                }
            }
        }
    }

}
