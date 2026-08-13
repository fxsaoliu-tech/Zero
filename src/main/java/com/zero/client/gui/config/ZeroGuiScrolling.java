package com.zero.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;
import java.util.List;

public class ZeroGuiScrolling extends GuiScrollingList {
    private final List<ZeroGuiContainer> container;
    private int selectedIndex = -1;
    private int mouseX, mouseY;

    public ZeroGuiScrolling(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight, List<ZeroGuiContainer> guiContainers) {
        super(client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        container = guiContainers;
    }

    @Override
    public int getSize() {
        return container.size();
    }

    @Override
    public void elementClicked(int index, boolean doubleClick) {
        this.selectedIndex = index;
        container.get(index).mouseClicked();
        for (int i = 0; i < container.size(); i++) {
            if (selectedIndex != i) {
                container.get(i).uncheck();
            }
        }
    }

    @Override
    public boolean isSelected(int index) {
        if (selectedIndex == index) {
            if (container.get(selectedIndex).getGui() == null) {
                return false;
            }
        }
        return selectedIndex == index;
    }

    @Override
    public void drawBackground() {

    }

    @Override
    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {

    }

    @Override
    public void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        container.get(slotIdx).drawSlot(entryRight, slotTop, slotBuffer, tess);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        for (ZeroGuiContainer container : container) {
            container.setMouse(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput(int x, int y) throws IOException {
        super.handleMouseInput(mouseX, mouseY);
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (selectedIndex != -1) {
            container.get(selectedIndex).keyTyped(typedChar, keyCode);
        }
    }
}
