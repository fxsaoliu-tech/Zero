package com.zero.network.gun;

import com.zero.network.PacketBase;
import com.zero.server.data.GunData;
import com.zero.server.data.PlayerData;
import com.zero.server.data.PlayerHandler;
import com.zero.server.item.ItemGun;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class PacketGunFireSelect extends PacketBase {

    public PacketGunFireSelect() {

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
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemGun) {
            ItemGun itemGun = (ItemGun) stack.getItem();
            PlayerData playerData = PlayerHandler.getPlayerData(player.getUniqueID());
            if (playerData != null) {
                GunData gunData = playerData.getGunData();
                if (gunData.isCanTrigger()) {
                    if (itemGun.getType().switchFireType(stack)) {
                        gunData.setTrigger();
                    }
                }
            }
        }
    }
}
