package com.zero.client.gui.gun;

import com.zero.Zero;
import com.zero.client.gui.gun.button.*;
import com.zero.network.gun.PacketGunRefitGun;
import com.zero.network.gun.PacketGunUnloadAttachment;
import com.zero.server.item.ItemGun;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.GunType;
import com.zero.server.type.IScope;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class GunAttachmentScreen extends GuiScreen {
    public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(Zero.MOD_ID, "textures/gui/refit_slot.png");
    public static final ResourceLocation TURN_PAGE_TEXTURE = new ResourceLocation(Zero.MOD_ID, "textures/gui/refit_turn_page.png");
    public static final ResourceLocation UNLOAD_TEXTURE = new ResourceLocation(Zero.MOD_ID, "textures/gui/refit_unload.png");
    public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(Zero.MOD_ID, "textures/gui/refit_slot_icons.png");


    public static final int ICON_UV_SIZE = 32;
    public static final int SLOT_SIZE = 18;
    private static final int INVENTORY_ATTACHMENT_SLOT_COUNT = 8;
    private static boolean HIDE_GUN_PROPERTY_DIAGRAMS = true;

    private int currentPage = 0;

    public GunAttachmentScreen() {
        RefitTransform.init();
    }


    public static int getSlotTextureXOffset(ItemStack gunItem, IAttachmentType attachmentType) {
        GunType gunType = GunType.getGunType(gunItem);
        if (gunType == null) {
            return -1;
        }
        if (!gunType.allowAttachmentType(attachmentType)) {
            return ICON_UV_SIZE * 6;
        }
        switch (attachmentType) {
            case GRIP:
                return 0;
            case LASER:
                return ICON_UV_SIZE;
            case MUZZLE:
                return ICON_UV_SIZE * 2;
            case SCOPE:
                return ICON_UV_SIZE * 3;
            case STOCK:
                return ICON_UV_SIZE * 4;
            case EXTENDED_MAG:
                return ICON_UV_SIZE * 5;
        }
        return -1;
    }

    public static int getSlotsTextureWidth() {
        return ICON_UV_SIZE * 7;
    }

    @Override
    public void initGui() {
        //清理组件
        this.buttonList.clear();
        //添加配件属性按钮
        addAttachmentTypeButtons();
        //添加背包配件按钮
        addInventoryAttachmentButtons();
        //添加枪械信息显示按钮
        if (HIDE_GUN_PROPERTY_DIAGRAMS) {
            addButton(new FlatColorButton(11, 11, 288, 16, "gui.zero.button.show", button -> {
                switchHideButton();
            }));
        } else {
            addButton(new FlatColorButton(11, 11, 288, 16, "gui.zero.button.showlist", button -> {
                switchHideButton();
            }));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!HIDE_GUN_PROPERTY_DIAGRAMS) {
            displayInfo();
        }
        for (GuiButton button : this.buttonList) {
            if (button instanceof IStackTooltip) {
                ItemStack stack = ((IStackTooltip) button).getTooltipStack();
                if (stack != null) {
                    this.renderToolTip(stack, mouseX, mouseY);
                }
            }
            if (button instanceof IComponentTooltip) {
                List<String> componentTooltip = ((IComponentTooltip) button).getTooltip();
                if (componentTooltip != null) {
                    this.drawHoveringText(componentTooltip, mouseX, mouseY);
                }
            }
        }
    }

    private void displayInfo() {
        Gui.drawRect(11, 27, 299, 200, 0xAF222222);
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        ItemStack itemStack = player.getHeldItemMainhand();
        if (itemStack.isEmpty() && !(itemStack.getItem() instanceof ItemGun)) {
            return;
        }
        ItemGun gun = (ItemGun) itemStack.getItem();
        GunType gunType = gun.getType();
        gui("gui.attachment.info.ammo", 30, gunType.ammo_amount, 999f, gun.getMagSize(itemStack));
        gui("gui.attachment.info.sprinting", 45, gunType.sprintShootTime, 0.5f, 0);
        IScope scope = gunType.getCurrentScope(itemStack);
        gui("gui.attachment.info.aim", 60, scope.getAimSpeed(), 1, 0);
        gui("gui.attachment.info.ammoSpeed", 75, gunType.speed, 20, 0);
        gui("gui.attachment.info.damage", 90, gunType.damage, 999, 0);

        AttachmentType muzzle = gunType.getAttachment(itemStack, IAttachmentType.MUZZLE);
        float spreadMultiplier = 0;
        if (muzzle != null) {
            spreadMultiplier = muzzle.spreadMultiplier;
        }
        gui("gui.attachment.info.baseSpread", 105, gunType.baseSpread, 10, -(spreadMultiplier * gunType.baseSpread));
        gui("gui.attachment.info.aimSpread", 120, gunType.aimSpread, 10, -(spreadMultiplier * gunType.aimSpread));

        gui("gui.attachment.info.recoilVertical", 135, gunType.recoilVertical, 10, gunType.getRecoilVertical(itemStack) - gunType.recoilVertical);
        gui("gui.attachment.info.recoilHorizontal", 150, gunType.recoilHorizontal, 10, gunType.getRecoilHorizontal(itemStack) - gunType.recoilHorizontal);

        gui("gui.attachment.info.shootRPM", 165, gunType.shootRpm, 1200, 0);

        Minecraft.getMinecraft().fontRenderer.drawString(I18n.format("gui.attachment.info.cue"), 13, 185, 0xFFFFFFFF);

    }

    private void gui(String name, int y, float value, float maxValue, float attribute) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        fr.drawString(I18n.format(name), 13, y, 0xFFFFFFFF);
        Gui.drawRect(80, y, 230, y + 8, 0xFF000000);

        float progress = (value + attribute) / maxValue;

        if (value >= maxValue || value + attribute >= maxValue) {
            progress = 1;
        }
        Gui.drawRect(80, y, (int) (80 + progress * 149), y + 8, 0xFFFFFFFF);

        String ccc = String.valueOf(value);

        fr.drawString(ccc, 235, y + 1, 0xFFFFFFFF);

        if (attribute > 0) {

            fr.drawString("§a(+" + String.format("%.2f", attribute) + ")", 238 + fr.getStringWidth(ccc), y + 1, 0xFF00FF00);
        } else if (attribute < 0) {
            fr.drawString("§c(" + String.format("%.2f", attribute) + ")", 238 + fr.getStringWidth(ccc), y + 1, 0xFF00FF00);
        }
    }


    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof IButtonAction) {
            ((IButtonAction) button).onClick(button);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void addInventoryAttachmentButtons() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (RefitTransform.getCurrentTransformType() == IAttachmentType.NONE || player == null) {
            return;
        }
        int startX = this.width - 30;
        int startY = 50;
        int pageStart = currentPage * INVENTORY_ATTACHMENT_SLOT_COUNT;
        int count = 0;
        int currentY = startY;
        InventoryPlayer inventory = player.inventory;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack inventoryItem = inventory.getStackInSlot(i);
            AttachmentType attachmentType = AttachmentType.getFromItemStack(inventoryItem);
            GunType gunType = GunType.getGunType(player.getHeldItemMainhand());
            if (attachmentType != null && gunType != null && attachmentType.enumAttachmentType == RefitTransform.getCurrentTransformType()) {
                if (!gunType.allowAttachment(inventoryItem)) {
                    continue;
                }
                count++;
                if (count <= pageStart) {
                    continue;
                }
                if (count > pageStart + INVENTORY_ATTACHMENT_SLOT_COUNT) {
                    continue;
                }
                InventoryAttachmentSlot button = new InventoryAttachmentSlot(startX, currentY, i, inventory, b -> {
                    int slotIndex = ((InventoryAttachmentSlot) b).getSlotIndex();
                    PacketGunRefitGun refitGun = new PacketGunRefitGun(slotIndex, inventory.currentItem, RefitTransform.getCurrentTransformType());
                    Zero.getPacketHandler().sendToServer(refitGun);
                });
                this.addButton(button);
                currentY = currentY + SLOT_SIZE;
            }
        }
        int totalPage = (count - 1) / INVENTORY_ATTACHMENT_SLOT_COUNT;
        RefitTurnPageButton turnPageButtonUp = new RefitTurnPageButton(startX, startY - 10, true, b -> {
            if (currentPage > 0) {
                currentPage--;
                initGui();
            }
        });
        RefitTurnPageButton turnPageButtonDown = new RefitTurnPageButton(startX, startY + SLOT_SIZE * INVENTORY_ATTACHMENT_SLOT_COUNT + 2, false, b -> {
            if (currentPage < totalPage) {
                currentPage++;
                initGui();
            }
        });
        if (currentPage < totalPage) {
            this.addButton(turnPageButtonDown);
        }
        if (currentPage > 0) {
            this.addButton(turnPageButtonUp);
        }
    }

    private void addAttachmentTypeButtons() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        GunType gunType = GunType.getGunType(player.getHeldItemMainhand());
        if (gunType == null) {
            return;
        }
        int startX = this.width - 30;
        int startY = 10;
        for (IAttachmentType type : IAttachmentType.values()) {
            if (type == IAttachmentType.NONE) {
                continue;
            }
            InventoryPlayer inventory = player.inventory;

            GunAttachmentSlotButton slotButton = new GunAttachmentSlotButton(startX, startY, inventory.currentItem, inventory, type, b -> {
                IAttachmentType buttonType = ((GunAttachmentSlotButton) b).getType();
                // 如果这个槽位不允许安装配件 ，则默认退回概览，不选中槽位。
                if (!((GunAttachmentSlotButton) b).isAllow()) {
                    if (RefitTransform.changeRefitScreenView(IAttachmentType.NONE)) {
                        this.initGui();
                    }
                    return;
                }
                // 点击的是当前选中的槽位，则退回概览
                if (RefitTransform.getCurrentTransformType() == buttonType && buttonType != IAttachmentType.NONE) {
                    if (RefitTransform.changeRefitScreenView(IAttachmentType.NONE)) {
                        this.initGui();
                    }
                    return;
                }
                // 切换选中的槽位。
                if (RefitTransform.changeRefitScreenView(buttonType)) {
                    this.initGui();
                }
            });
            if (RefitTransform.getCurrentTransformType() == type) {
                slotButton.setSelected(true);
                GunAttachmentUnloadButton unloadButton = new GunAttachmentUnloadButton(startX + 5, startY + SLOT_SIZE + 2, type, b -> {
                    ItemStack attachmentItem = slotButton.getAttachmentItem();
                    if (!attachmentItem.isEmpty()) {
                        int freeSlot = inventory.getFirstEmptyStack();
                        if (freeSlot != -1) {
                            PacketGunUnloadAttachment message = new PacketGunUnloadAttachment(inventory.currentItem, type);
                            Zero.getPacketHandler().sendToServer(message);
                        } else {
                            player.sendMessage(new TextComponentTranslation("gui.gun_refit.unload.no_space"));
                        }
                    }
                });
                if (!slotButton.getAttachmentItem().isEmpty()) {
                    this.addButton(unloadButton);
                }
            }
            this.addButton(slotButton);
            startX = startX - SLOT_SIZE;
        }
    }


    private void switchHideButton() {
        HIDE_GUN_PROPERTY_DIAGRAMS = !HIDE_GUN_PROPERTY_DIAGRAMS;
        this.initGui();
    }

}
