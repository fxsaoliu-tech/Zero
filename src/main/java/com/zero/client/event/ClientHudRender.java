package com.zero.client.event;

import com.zero.Zero;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.client.debug.Cmd;
import com.zero.client.util.render.RenderHelper;
import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientHudRender {

    public ClientHudRender() {
        Zero.eventRegister(this);
        ClientCommandHandler.instance.registerCommand(new Cmd());
    }


    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            ItemZeroStateMachine stateMachine = ZeroClientPlayer.getStateMachine();
            if (stateMachine != null) {
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {


        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;


        Minecraft mc = Minecraft.getMinecraft();


        ScaledResolution sr = new ScaledResolution(mc);
        renderGunAmmo(sr);

        ItemZeroStateMachine stateMachine = ZeroClientPlayer.getStateMachine();
        if (stateMachine != null && !stateMachine.isCloseCrossHairs()) {
            renderCrosshair(sr);
        }
        if (ClientTickEvent.hitTime > 0) {
            this.renderX(sr);
        }
    }

    private static final ResourceLocation CROSSHAIR = new ResourceLocation(Zero.MOD_ID, "textures/crosshair/cross.png");
    private static final ResourceLocation X = new ResourceLocation(Zero.MOD_ID, "textures/crosshair/x.png");


    public void renderCrosshair(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();

        int width = sr.getScaledWidth() / 2;
        int height = sr.getScaledHeight() / 2;

        int size = 8; // 屏幕显示大小

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);

        mc.getTextureManager().bindTexture(CROSSHAIR);

        RenderHelper.drawImage(width - size / 2, height - size / 2, size, size, 128, 128);

        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    public void renderX(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();

        int width = sr.getScaledWidth() / 2;
        int height = sr.getScaledHeight() / 2;

        int size = 8; // 屏幕显示大小

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);

        mc.getTextureManager().bindTexture(X);

        RenderHelper.drawImage(width - size / 2, height - size / 2, size, size, 128, 128);

        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }


    public void renderGunAmmo(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();

        int width = sr.getScaledWidth() - 10;
        int height = sr.getScaledHeight();

        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack item = player.getHeldItemMainhand();
        if (item.isEmpty()) {
            return;
        }
        if (!(item.getItem() instanceof ItemGun)) {
            return;
        }
        ItemGun gun = (ItemGun) item.getItem();
        //背景
        GlStateManager.pushMatrix();
        Gui.drawRect(width - 75, height - 80, width + 10, height - 50, 0xAA000000);
        GlStateManager.popMatrix();
        //开火模式
        String fire = gun.getType().getFireType(item).getId();
        int fireTextWidth = mc.fontRenderer.getStringWidth(fire);
        GlStateManager.pushMatrix();
        mc.fontRenderer.drawString("§6" + fire, width - fireTextWidth, height - 75, 0xffffff);
        GlStateManager.popMatrix();
        //弹药显示
        int currentAmmo = gun.getCurrentAmmo(item);
        String ammoColor = currentAmmo < 1 ? "§c" : "§f";
        String ammo = ammoColor + currentAmmo + " §b/§f " + gun.getMaxMagSize(item);
        float scale = 1.5F;
        int textWidth = mc.fontRenderer.getStringWidth(ammo);
        int x = (int) ((width - textWidth * scale) / scale);
        int y = (int) ((height - 62) / scale);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0F);
        mc.fontRenderer.drawString(ammo, x, y, 0xffffff);
        GlStateManager.popMatrix();
    }
}
