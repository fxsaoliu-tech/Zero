package com.zero.client.gui.gun.button;

import com.zero.client.gui.gun.GunAttachmentScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.List;

public class RefitTurnPageButton extends GuiButton implements IButtonAction, IComponentTooltip {
    private final boolean isUpPage;
    private final IButtonAction action;

    public RefitTurnPageButton(int x, int y, boolean isUpPage, IButtonAction buttonAction) {
        super(buttonAction.hashCode(), x, y, 18, 8, "");
        this.isUpPage = isUpPage;
        this.action = buttonAction;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        mc.getTextureManager().bindTexture(GunAttachmentScreen.TURN_PAGE_TEXTURE);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        int yOffset = isUpPage ? 0 : 80;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (this.isMouseOver()) {
            drawScaledCustomSizeModalRect(x, y, 0, yOffset, 180, 80, width, height, 180, 160);
        } else {
            drawScaledCustomSizeModalRect(x + 1, y + 1, 10, yOffset + 10, 160, 60, width - 2, height - 2, 180, 160);
        }


        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    @Override
    public void onClick(GuiButton button) {
        action.onClick(button);
    }

    @Override
    public List<String> getTooltip() {
        if (this.isMouseOver()) {
            String key = isUpPage ? "tooltip.zero.page.previous" : "tooltip.zero.page.next";
            return Collections.singletonList(I18n.format(key));
        }
        return null;
    }
}
