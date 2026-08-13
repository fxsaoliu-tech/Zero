package com.zero.client.model.display;

import com.zero.server.file.FileList;

import java.util.List;

public class TextShowText extends TxtRead {

    private boolean shadow;

    private String text;

    private int color = 0xFFFFFF;

    private float scale = 1f;

    private int textLight = 15;

    private Align align = Align.CENTER;


    public TextShowText(List<String> text, FileList fileList) {
        super(text, fileList);
    }

    @Override
    public void read(String[] split, FileList fileList) {
        text = Read(split, "text", text);
        scale = Read(split, "scale", scale, fileList);
        textLight = Read(split, "textLight", textLight, fileList);
        color = ReadColor(split, "color", color);
        shadow = Read(split, "shadow", shadow, fileList);

        String alignType = Read(split, "align", "");

        if (alignType != null) {
            align = Align.getAlignType(alignType);
        }

    }

    public Align getAlign() {
        return align;
    }

    public float getScale() {
        return scale;
    }

    public int getColor() {
        return color;
    }

    public int getTextLight() {
        return textLight;
    }

    public boolean isShadow() {
        return shadow;
    }

    public String getText() {
        return text;
    }
}
