package com.zero.client.gui.gun.button;

import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public interface IStackTooltip {
    /**
     * 添加此接口，会调用此渲染文本提示
     * 需要渲染文本提示的物品
     */
    ItemStack getTooltipStack();

}
