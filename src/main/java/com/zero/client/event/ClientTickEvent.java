package com.zero.client.event;

import com.zero.Zero;
import com.zero.api.client.KeepingItemRenderer;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.animation.gun.RecoilSystem;
import com.zero.client.ClientProxy;
import com.zero.client.gui.config.ZeroConfig;
import com.zero.client.gui.gun.RefitTransform;
import com.zero.server.data.PlayerHandler;
import com.zero.server.item.ItemZero;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ClientTickEvent {
    public static final RecoilSystem RECOIL_SYSTEM = new RecoilSystem();
    public static float partialTicks;

    public static int hitTime = 0;

    public ClientTickEvent() {
        Zero.eventRegister(this);
        new ClientHudRender();
    }


    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                Zero.getPacketHandler().handleClientPackets();
                ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
                if (ItemZero.isZero(stack)) {
                    ItemZeroStateMachine machine = ZeroClientPlayer.getStateMachine();
                    if (machine != null) {
                        machine.updateTick(ClientProxy.getMinecraft().player);
                    }
                }
                if (ClientProxy.getMinecraft().currentScreen != null) {
                    Zero.tick++;
                } else {
                    Zero.tick = 0;
                }
                EntityPlayer player = ClientProxy.getMinecraft().player;
                if (player != null) {
                    RECOIL_SYSTEM.update(player);
                }
                if (hitTime > 0) {
                    hitTime--;
                }
                break;
        }
    }

    @SubscribeEvent
    public void renderWorldLast(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || !ZeroConfig.debug) return;

        double x = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double y = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        GlStateManager.translate(-x, -y, -z);
        PlayerHandler.ClientTick();

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        ZeroClientPlayer player = ZeroClientPlayer.getPlayer();
        if (event.phase == TickEvent.Phase.START) {
            partialTicks = event.renderTickTime;
            if (player != null) {
                player.onUpdateRender();
            }
            RefitTransform.tickInterpolation();
            if (player != null) {
                ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
                if (ItemZero.isZero(stack)) {
                    ItemZeroStateMachine machine = player.getItemZero();
                    if (machine != null) {
                        machine.updateRender((EntityPlayer) player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ZeroClientPlayer.getPlayer().clearDate();
    }


}
