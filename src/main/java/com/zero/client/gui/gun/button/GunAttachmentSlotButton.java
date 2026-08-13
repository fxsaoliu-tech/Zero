package com.zero.client.gui.gun.button;

import com.zero.client.gui.gun.GunAttachmentScreen;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public class GunAttachmentSlotButton extends GuiButton implements IButtonAction, IStackTooltip {

    private final IButtonAction action;
    private final IAttachmentType type;
    private final IInventory inventory;
    private final int gunItemIndex;
    private final String nameKey;
    private boolean selected = false;
    private ItemStack attachmentItem = ItemStack.EMPTY;

    public GunAttachmentSlotButton(int x, int y, int gunItemIndex, IInventory inventory, IAttachmentType type, IButtonAction action) {
        super(type.hashCode(), x, y, GunAttachmentScreen.SLOT_SIZE, GunAttachmentScreen.SLOT_SIZE, "");
        this.type = type;
        this.action = action;
        this.inventory = inventory;
        this.gunItemIndex = gunItemIndex;
        this.nameKey = String.format("tooltip.zero.attachment.%s", type.name().toLowerCase(Locale.US));
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        if (this.isMouseOver()) {
            FontRenderer font = mc.fontRenderer;
            int yOffset = this.y + 20;
            if (this.selected && !this.attachmentItem.isEmpty()) {
                yOffset = this.y + 30;
            }
            String text = I18n.format(this.nameKey);
            drawCenteredString(font, text, this.x + this.width / 2, yOffset, 0xFFFFFF);
        }
        GlStateManager.color(1, 1, 1, 1);

        ItemStack gunItem = inventory.getStackInSlot(gunItemIndex);
        GunType iGun = GunType.getGunType(gunItem);
        if (iGun == null) {
            return;
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        mc.getTextureManager().bindTexture(GunAttachmentScreen.SLOT_TEXTURE);

        if (hovered || selected) {
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, GunAttachmentScreen.SLOT_SIZE, GunAttachmentScreen.SLOT_SIZE);
        } else {
            drawModalRectWithCustomSizedTexture(x + 1, y + 1, 1, 1, width - 2, height - 2, GunAttachmentScreen.SLOT_SIZE, GunAttachmentScreen.SLOT_SIZE);
        }

        this.attachmentItem = iGun.getAttachmentItemStack(gunItem, type);

        RenderItem itemRender = mc.getRenderItem();

        if (!attachmentItem.isEmpty()) {
            itemRender.renderItemAndEffectIntoGUI(attachmentItem, x + 1, y + 1);
        } else {
            mc.getTextureManager().bindTexture(GunAttachmentScreen.ICONS_TEXTURE);
            int xOffset = GunAttachmentScreen.getSlotTextureXOffset(gunItem, type);
            drawScaledCustomSizeModalRect(x + 2, y + 2, xOffset, 0, GunAttachmentScreen.ICON_UV_SIZE, GunAttachmentScreen.ICON_UV_SIZE, width - 4, height - 4, GunAttachmentScreen.getSlotsTextureWidth(), GunAttachmentScreen.ICON_UV_SIZE);
        }
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public IAttachmentType getType() {
        return type;
    }

    public ItemStack getAttachmentItem() {
        ItemStack gunItem = inventory.getStackInSlot(gunItemIndex);
        GunType gunType = GunType.getGunType(gunItem);
        if (gunType == null) {
            return ItemStack.EMPTY;
        }
        return gunType.getAttachmentItemStack(gunItem, type);
    }

    public boolean isAllow() {
        ItemStack gunItem = inventory.getStackInSlot(gunItemIndex);
        GunType gunType = GunType.getGunType(gunItem);
        if (gunType == null) {
            return false;
        }
        return gunType.allowAttachmentType(type);
    }

    @Override
    public void onClick(GuiButton button) {
        action.onClick(this);
    }

    @Override
    public ItemStack getTooltipStack() {
        if (this.isMouseOver() && !attachmentItem.isEmpty()) {
            return attachmentItem;
        }
        return null;
    }
}
