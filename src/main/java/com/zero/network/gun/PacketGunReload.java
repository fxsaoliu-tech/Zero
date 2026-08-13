package com.zero.network.gun;

import com.zero.network.PacketBase;
import com.zero.server.data.GunData;
import com.zero.server.data.PlayerData;
import com.zero.server.data.PlayerHandler;
import com.zero.server.item.ItemGun;
import com.zero.server.type.GunType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class PacketGunReload extends PacketBase {
    private long reloadEndTime;

    public PacketGunReload(){

    }

    public PacketGunReload(long reloadEndTime) {
        this.reloadEndTime = reloadEndTime;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeLong(reloadEndTime);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        reloadEndTime = data.readLong();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemGun)) {
            return;
        }
        PlayerData playerData = PlayerHandler.getPlayerData(player.getUniqueID());
        if (playerData == null) {
            return;
        }
        GunData gunData = playerData.getGunData();
        ItemGun gun = (ItemGun) stack.getItem();
        GunType gunType = gun.getType();
        long reloadEndTime = (long) (gun.getCurrentAmmo(player.getHeldItemMainhand()) < 1 ? gunType.reloadEmptyTime : gunType.reloadTacticalTime) * 1000;
        if (reloadEndTime == this.reloadEndTime) {
            if (gunData.canReload()) {
                if (gun.reload(stack, player.world, player, player.inventory, player.isCreative())) {
                    gunData.beginReload(reloadEndTime);
                }
            }
        }
    }
}
