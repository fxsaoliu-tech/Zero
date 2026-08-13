package com.zero.client.gui.gun.button;

import com.zero.client.gui.gun.GunAttachmentScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class InventoryAttachmentSlot extends GuiButton implements IButtonAction, IStackTooltip {
    private final int slotIndex;
    private final InventoryPlayer inventory;
    private final IButtonAction buttonAction;

    public InventoryAttachmentSlot(int x, int y, int slotIndex, InventoryPlayer inventory, IButtonAction buttonAction) {
        super(slotIndex + inventory.hashCode(), x, y, 18, 18, "");
        this.slotIndex = slotIndex;
        this.inventory = inventory;
        this.buttonAction = buttonAction;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        mc.getTextureManager().bindTexture(GunAttachmentScreen.SLOT_TEXTURE);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (this.isMouseOver()) {
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 18, 18);
        } else {
            drawModalRectWithCustomSizedTexture(x + 1, y + 1, 1, 1, width - 2, height - 2, 18, 18);
        }

        // 渲染物品
        ItemStack stack = inventory.getStackInSlot(slotIndex);
        if (!stack.isEmpty()) {
            RenderItem itemRender = mc.getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(stack, x + 1, y + 1);
        }
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public void onClick(GuiButton button) {
        buttonAction.onClick(button);
    }

    @Override
    public ItemStack getTooltipStack() {
        if (this.isMouseOver() && 0 <= this.slotIndex && this.slotIndex < this.inventory.getSizeInventory()) {
            return this.inventory.getStackInSlot(this.slotIndex);
        }
        return null;
    }
}
