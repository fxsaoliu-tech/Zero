package com.zero.api.client;

import com.zero.client.ClientProxy;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.server.item.ItemZero;
import net.minecraft.item.ItemStack;

public interface ZeroClientPlayer {

    ItemZeroStateMachine getItemZero();

    void onUpdateRender();

    void clearDate();

    /**
     * ItemInHandRenderer 通过 Mixin 的方式实现了此接口。
     *
     * @return 返回 ItemInHandRenderer 实例
     */
    static ZeroClientPlayer getPlayer() {
        return (ZeroClientPlayer) ClientProxy.getMinecraft().player;
    }

    static ItemZeroStateMachine getStateMachine() {
        ZeroClientPlayer player = getPlayer();
        if (player == null) return null;
        KeepingItemRenderer renderer = KeepingItemRenderer.getRenderer();
        if (renderer == null) return null;
        ItemStack stack = renderer.getCurrentItem();
        if (!ItemZero.isZero(stack)) {
            return null;
        }
        return player.getItemZero();
    }

}
