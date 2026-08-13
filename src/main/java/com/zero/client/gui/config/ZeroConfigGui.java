package com.zero.client.gui.config;

import com.zero.Zero;
import com.zero.client.util.render.RenderHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZeroConfigGui extends GuiScreen {
    private static final ResourceLocation LOGO = new ResourceLocation(Zero.MOD_ID, "zero_logo.png");
    private ZeroGuiScrolling scrolling;
    private final List<ZeroGuiContainer> containers = new ArrayList<>();

    public ZeroConfigGui() {

    }

    @Override
    public void initGui() {
        super.initGui();
        containers.clear();
        //文件系列
        addContainerTitle("gui.zero.config.title.file");
        addContainerButton("gui.zero.config.button.file", ZeroConfig.isUpdateJson, "false", container -> ZeroConfig.isUpdateJson = container.getBoolean());
        //debug
        addContainerTitle("gui.zero.config.title.debug");
        addContainerButton("gui.zero.config.button.debug", ZeroConfig.debug, "false", container -> ZeroConfig.debug = container.getBoolean());
        //渲染
        addContainerTitle("gui.zero.config.title.render");
        addContainerTextFloat("gui.zero.config.text.render.beam_length", ZeroConfig.beam_length, "2.0", container -> ZeroConfig.beam_length = container.getFloat());
        addContainerTextFloat("gui.zero.config.text.render.beam_width", ZeroConfig.beam_width, "0.02", container -> ZeroConfig.beam_width = container.getFloat());

        //倍镜
        addContainerTitle("gui.zero.config.title.scope");
        addContainerTextFloat("gui.zero.config.text.scope.sensitivityMultiplier", ZeroConfig.sensitivityMultiplier, "1.0", container -> ZeroConfig.sensitivityMultiplier = container.getFloat());
        addContainerTextFloat("gui.zero.config.text.scope.coefficient", ZeroConfig.coefficient, "1.33", container -> ZeroConfig.coefficient = container.getFloat());
        addContainerButton("gui.zero.config.button.aim", ZeroConfig.aim, "false", container -> ZeroConfig.aim = container.getBoolean());
        scrolling = new ZeroGuiScrolling(mc, this.width, this.height, 56, this.height, 0, 22, this.width, this.height, containers);
    }


    //监听键盘
    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (ZeroGuiContainer container : containers) {
            container.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        for (ZeroGuiContainer container : containers) {
            if (container.getGui() != null) {
                container.getGui().sava(container);
            }
        }
        ZeroConfig.save();
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        scrolling.handleMouseInput(0, 0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        // 全屏半透明黑色背景 50%
        if (mc.world != null && mc.player != null) {
            GlStateManager.pushMatrix();
            mc.getTextureManager().bindTexture(LOGO);
            RenderHelper.drawImage(0, 0, width, height, 1920, 1080);
            GlStateManager.popMatrix();
            drawRect(0, 0, width, height, 0x80000000);
        } else {
            this.drawDefaultBackground();
        }
        // 标题区域底线
        String title = I18n.format("gui.zero.config.title");

        drawCenteredString(fontRenderer, title, width / 2, 30, 0xFFFFFF);

        drawRect(0, 50, width, 55, 0xFFFFFFFF);

        scrolling.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void addContainerTitle(String title) {
        this.containers.add(new ZeroGuiContainer(title));
    }

    private void addContainerButton(String name, boolean value, String defaultValue, IGuiClose close) {
        this.containers.add(new ZeroGuiContainer(name, value, defaultValue, close));
    }

    private void addContainerText(String name, String value, String defaultValue, IGuiClose close) {
        this.containers.add(new ZeroGuiContainer(name, value, defaultValue, close));
    }

    private void addContainerTextFloat(String name, float value, String defaultValue, IGuiClose close) {
        this.containers.add(new ZeroGuiContainer(name, value, defaultValue, close));
    }

}