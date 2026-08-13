package com.zero.client.model.display;

import com.zero.server.file.FileList;

import java.util.List;

public class MuzzleFlashText extends TxtRead {
    private String muzzleFlash;
    private float scale;

    public MuzzleFlashText(List<String> text, FileList fileList) {
        super(text, fileList);
    }

    @Override
    public void read(String[] split, FileList fileList) {
        muzzleFlash = Read(split,"muzzleFlash",muzzleFlash);
        scale = Read(split, "scale", scale, fileList);
    }

    public float getScale() {
        return scale;
    }

    public String getMuzzleFlash() {
        return muzzleFlash;
    }
}
