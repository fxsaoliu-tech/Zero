package com.zero.server.type.mode;

public enum IAttachmentType {
    SCOPE("scope"),     // 瞄准镜
    MUZZLE("muzzle"),   // 枪口
    STOCK("stock"),     // 枪托
    GRIP("grip"),       // 握把
    LASER("laser"), // 激光
    EXTENDED_MAG("extended_mag"),         // 弹夹
    NONE("none");

    private final String key;

    IAttachmentType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static IAttachmentType getAttachmentType(String s) {
        for (IAttachmentType type : values()) {
            if (type.key.equals(s) || type.name().equalsIgnoreCase(s)) {
                return type;
            }
        }
        return null;
    }
}