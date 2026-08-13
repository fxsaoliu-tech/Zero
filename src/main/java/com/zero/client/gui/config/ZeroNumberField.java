package com.zero.client.gui.config;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class ZeroNumberField extends GuiTextField {

    public ZeroNumberField(int id, FontRenderer font, int x, int y, int width, int height) {
        super(id, font, x, y, width, height);
    }


    @Override
    public void writeText(String text) {
        if (text.matches("[0-9.]*")) {
            super.writeText(text);
        }
    }
}
