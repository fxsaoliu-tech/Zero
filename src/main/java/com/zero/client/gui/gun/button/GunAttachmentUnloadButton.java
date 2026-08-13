package com.zero.client.gui.gun.button;

import com.zero.client.gui.gun.GunAttachmentScreen;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.List;

public class GunAttachmentUnloadButton extends GuiButton implements IButtonAction, IComponentTooltip {
    private final IButtonAction action;

    public GunAttachmentUnloadButton(int x, int y, IAttachmentType type, IButtonAction action) {
        super(type.hashCode() + 1, x - 3, y - 4, 16, 16, "");
        this.action = action;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        // ======================
        // 绑定纹理
        // ======================
        mc.getTextureManager().bindTexture(GunAttachmentScreen.UNLOAD_TEXTURE);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        // ======================
        // 渲染按钮
        // ======================
        if (this.isMouseOver()) {
            drawScaledCustomSizeModalRect(x, y, 0, 0, 80, 80, width, height, 160, 80);
        } else {
            drawScaledCustomSizeModalRect(x, y, 80, 0, 80, 80, width, height, 160, 80);
        }
        // ======================
        // 恢复状态
        // ======================
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
            return Collections.singletonList(I18n.format("tooltip.zero.refit.unload"));
        }
        return null;
    }
}
