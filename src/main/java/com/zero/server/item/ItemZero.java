package com.zero.server.item;

import com.zero.api.PlayerItemZeroDataHolder;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.server.type.InfoType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ItemZero {

    //获取属性
    InfoType getType();

    //包含动画和驱动逻辑
    ItemZeroStateMachine getStateMachine(PlayerItemZeroDataHolder holder);

    @SideOnly(Side.CLIENT)
    static boolean isZero(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.getItem() instanceof ItemZero;
    }
}
