package com.zero.network.gun;

import com.zero.Zero;
import com.zero.network.PacketBase;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.IAttachmentType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class PacketGunRefitGun extends PacketBase {
    private int slotIndex;
    private int mainSlotIndex;
    private IAttachmentType type;

    public PacketGunRefitGun() {

    }

    public PacketGunRefitGun(int slotIndex, int mainSlotIndex, IAttachmentType type) {
        this.slotIndex = slotIndex;
        this.mainSlotIndex = mainSlotIndex;
        this.type = type;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(slotIndex);
        data.writeInt(mainSlotIndex);
        writeUTF(data, type.getKey());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        slotIndex = data.readInt();
        mainSlotIndex = data.readInt();
        type = IAttachmentType.getAttachmentType(readUTF(data));
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {
        if (type == null) {
            return;
        }
        // ✔ 枪
        ItemStack gun = player.inventory.getStackInSlot(mainSlotIndex);
        if (gun.isEmpty()) return;
        // ✔ 配件
        ItemStack attachment = player.inventory.getStackInSlot(slotIndex);
        if (attachment.isEmpty()) return;
        // ✔ 获取枪类型
        GunType gunType = GunType.getGunType(gun);
        if (gunType == null) return;
        // ✔ 校验是否允许该类型
        if (!gunType.allowAttachmentType(type)) return;
        // ✔ 校验是否兼容该配件
        if (!gunType.allowAttachment(attachment)) return;
        // ✔ 取出原来的配件（如果有）
        ItemStack oldAttachment = gunType.getAttachmentItemStack(gun, type);
        // ✔ 装配新配件
        gunType.addAttachment(gun, type, attachment.copy());
        // ✔ 从背包移除新配件
        player.inventory.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
        // ✔ 如果原来有配件，放回背包
        if (!oldAttachment.isEmpty()) {
            int freeSlot = player.inventory.getFirstEmptyStack();
            if (freeSlot != -1) {
                player.inventory.setInventorySlotContents(freeSlot, oldAttachment.copy());
            } else {
                // 背包满 → 掉落
                player.dropItem(oldAttachment, false);
            }
        }
        // ✔ 同步背包
        player.inventory.markDirty();
        Zero.getPacketHandler().sendToPlayer(new PacketGunRefreshRefitScreen(), player);
    }
}
