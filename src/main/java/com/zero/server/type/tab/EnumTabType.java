package com.zero.server.type.tab;

public enum EnumTabType {
    //枪械细分
    PISTOL("pistol"),        // 手枪
    SMG("smg"),           // 冲锋枪
    RIFLE("rifle"),         // 步枪
    SNIPER("sniper"),        // 狙击枪
    MACHINE("machine"),       // 机枪
    SHOTGUN("shotGun"),        // 散弹枪

    //子弹
    BULLET("bullet"),

    //配件系统
    muzzle("muzzle"),
    scope("scope"),
    laser("laser"),
    extended_mag("extended_mag"),
    grip("grip"),
    stock("stock");


    private String name;

    EnumTabType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EnumTabType getEnumInfoType(String name) {
        for (EnumTabType type : EnumTabType.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}
