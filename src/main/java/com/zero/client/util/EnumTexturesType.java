package com.zero.client.util;

public enum EnumTexturesType {
    ATTACHMENT("attachments"),
    BULLET("bullets"),
    GUN("guns"),
    MUZZLE("muzzles"),
    SHELL("shells");

    private String name;

    EnumTexturesType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
