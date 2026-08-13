package com.zero.network.gun;

import com.zero.Zero;
import com.zero.network.PacketBase;
import com.zero.server.item.ItemGun;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.IAttachmentType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class PacketGunUnloadAttachment extends PacketBase {
    private int index;
    private IAttachmentType attachment;

    public PacketGunUnloadAttachment() {
    }

    public PacketGunUnloadAttachment(int index, IAttachmentType attachmentType) {
        this.index = index;
        this.attachment = attachmentType;
    }


    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(index);
        data.writeByte(attachment.ordinal());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        index = data.readInt();
        attachment = IAttachmentType.values()[data.readByte()];
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {
        ItemStack gunItem = player.inventory.getStackInSlot(index);

        if (gunItem.isEmpty()) return;

        GunType gunType = GunType.getGunType(gunItem);
        if (gunType == null) return;

        // ✔ 先确认是否有配件
        ItemStack attachmentItem = gunType.getAttachmentItemStack(gunItem, attachment);

        if (attachmentItem.isEmpty()) return;

        // ✔ 再找空格
        int freeSlot = player.inventory.getFirstEmptyStack();
        if (freeSlot == -1) return;

        // 如果卸载的是扩容弹匣，吐出所有子弹
        if (attachment == IAttachmentType.EXTENDED_MAG) {
            ((ItemGun) gunItem.getItem()).unload(gunItem, player.world, player, player.inventory, player.isCreative());
        }

        // ✔ 执行卸载
        gunType.removeAttachment(gunItem, attachment);

        // ✔ 放入背包
        player.inventory.setInventorySlotContents(freeSlot, attachmentItem.copy());

        Zero.getPacketHandler().sendToPlayer(new PacketGunRefreshRefitScreen(), player);
    }
}
