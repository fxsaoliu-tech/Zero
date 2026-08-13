package com.zero.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;

public class ZeroGuiContainer {
    //标签可名称
    private String title;
    //设置
    private Minecraft mc;
    private int mouseX;
    private int mouseY;
    private float partialTicks;
    //按钮
    private GuiButton button;
    //文本框
    private GuiTextField textField;
    //默认值
    private String defaultType;
    //恢复默认的按钮
    private GuiButton defaultButton;
    //关闭监听器
    private IGuiClose gui;

    public ZeroGuiContainer(String title) {
        this.title = I18n.format(title);
        this.mc = Minecraft.getMinecraft();
    }

    public ZeroGuiContainer(String title, boolean buttonText, String defaultType, IGuiClose gui) {
        this(title);
        this.button = new GuiButton(title.hashCode(), 0, 0, 80, 20, String.valueOf(buttonText));
        this.defaultButton = new GuiButton(title.hashCode() + 1, 0, 0, 30, 20, I18n.format("gui.zero.config.button.defult"));
        this.defaultType = defaultType;
        this.gui = gui;
    }

    public ZeroGuiContainer(String title, String text, String defaultType, IGuiClose gui) {
        this(title);
        this.textField = new GuiTextField(title.hashCode(), mc.fontRenderer, 0, 0, 80, 20);
        this.textField.setText(text);
        this.textField.setMaxStringLength(8);
        this.defaultButton = new GuiButton(title.hashCode() + 1, 0, 0, 30, 20, I18n.format("gui.zero.config.button.defult"));
        this.defaultType = defaultType;
        this.gui = gui;
    }

    public ZeroGuiContainer(String title, float value, String defaultType, IGuiClose gui) {
        this(title);
        this.textField = new ZeroNumberField(title.hashCode(), mc.fontRenderer, 0, 0, 80, 20);
        this.textField.setText(String.valueOf(value));
        this.textField.setMaxStringLength(8);
        this.defaultButton = new GuiButton(title.hashCode() + 1, 0, 0, 30, 20, I18n.format("gui.zero.config.button.defult"));
        this.defaultType = defaultType;
        this.gui = gui;
    }

    public void setMouse(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
    }

    public void drawSlot(int entryRight, int slotTop, int slotHeight, Tessellator tess) {
        FontRenderer font = mc.fontRenderer;
        if (button == null && textField == null) {
            font.drawString(title, (entryRight / 2) - (font.getStringWidth(title) / 2), slotTop + 5, 0xFFFFFF);
            return;
        }
        font.drawString(title, 10, slotTop + 5, 0xFFFFFF);

        if (button != null) {
            button.x = entryRight - 110;
            button.y = slotTop - 1;
            button.drawButton(mc, mouseX, mouseY, partialTicks);
        }
        if (textField != null) {
            textField.x = entryRight - 110;
            textField.y = slotTop - 1;
            textField.drawTextBox();
            textField.updateCursorCounter();
        }
        if (defaultButton != null) {
            defaultButton.x = entryRight - 30;
            defaultButton.y = slotTop - 1;
            defaultButton.drawButton(mc, mouseX, mouseY, partialTicks);

            if (this.button != null) {
                defaultButton.enabled = !button.displayString.equalsIgnoreCase(defaultType);
            }
            if (this.textField != null) {
                defaultButton.enabled = !textField.getText().equalsIgnoreCase(defaultType);
            }
        }
    }

    //文本框和按钮被点击
    public void mouseClicked() {
        if (button != null) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                button.playPressSound(mc.getSoundHandler());
                switch (button.displayString.toLowerCase()) {
                    case "false":
                        button.displayString = "true";
                        break;
                    case "true":
                        button.displayString = "false";
                        break;
                }
            }
        }
        if (defaultButton != null) {
            if (defaultButton.mousePressed(mc, mouseX, mouseY)) {
                if (button != null) {
                    button.displayString = this.defaultType;
                }
                if (textField != null) {
                    textField.setText(this.defaultType);
                }
                defaultButton.playPressSound(mc.getSoundHandler());
            }
        }
        if (textField != null) {
            if (textField.mouseClicked(mouseX, mouseY, 0)) {

            }
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (textField != null) {
            if (textField.isFocused()) {
                textField.textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    public void uncheck() {
        if (textField != null) {
            textField.setFocused(false);
        }
    }

    public boolean getBoolean() {
        if (button != null) {
            return Boolean.parseBoolean(button.displayString);
        }
        return false;
    }

    public String getString() {
        if (textField != null) {
            return textField.getText();
        }
        return "";
    }

    public float getFloat() {
        if (textField != null) {
            return Float.parseFloat(textField.getText());
        }
        return 0;
    }

    public int getInteger() {
        if (textField != null) {
            return Integer.parseInt(textField.getText());
        }
        return 0;
    }

    public IGuiClose getGui() {
        return gui;
    }
}
