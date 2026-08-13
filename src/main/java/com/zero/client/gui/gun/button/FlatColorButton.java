package com.zero.client.gui.gun.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.List;

public class FlatColorButton extends GuiButton implements IButtonAction, IComponentTooltip {
    private final IButtonAction action;
    /**
     * 是否选中
     */
    private boolean selected;
    /**
     * Tooltip
     */
    private List<String> tooltips;
    /**
     * 颜色
     */
    private static final int BG_COLOR = 0xAF222222;
    private static final int BG_SELECTED = 0xAF3A3A3A;
    private static final int BORDER_COLOR = 0xFFF3EFE0;
    private static final int TEXT_COLOR = 0xF3EFE0;
    private static final int DISABLED_TEXT = 0x808080;

    public FlatColorButton(int x, int y, int widthIn, int heightIn, String name, IButtonAction action) {
        super(name.hashCode(), x, y, widthIn, heightIn, I18n.format(name));
        this.action = action;
    }


    public FlatColorButton setTooltips(String... tips) {
        this.tooltips = Arrays.asList(tips);
        return this;
    }

    public FlatColorButton setTooltips(List<String> tips) {
        this.tooltips = tips;
        return this;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        FontRenderer font = mc.fontRenderer;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        //背景
        Gui.drawRect(x, y, x + width, y + height, selected ? BG_SELECTED : BG_COLOR);
        //选择中
        if (hovered) {
            Gui.drawRect(x, y, x + width, y + 1, BORDER_COLOR);
            Gui.drawRect(x, y + height - 1, x + width, y + height, BORDER_COLOR);
            Gui.drawRect(x, y + 1, x + 1, y + height - 1, BORDER_COLOR);
            Gui.drawRect(x + width - 1, y + 1, x + width, y + height - 1, BORDER_COLOR);
        }
        //文本
        int color;
        if (!enabled) {
            color = DISABLED_TEXT;
        } else {
            color = TEXT_COLOR;
        }
        drawCenteredString(font, displayString, x + width / 2, y + (height - 8) / 2, color);
    }

    @Override
    public void onClick(GuiButton button) {
        action.onClick(button);
    }

    @Override
    public List<String> getTooltip() {
        if (this.isMouseOver() && tooltips != null) {
            tooltips.replaceAll(I18n::format);
            return tooltips;
        }
        return null;
    }
}
