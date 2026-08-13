package com.zero.server.type.mode;

/**
 * 枪械射击模式
 * 用于定义武器的开火行为类型（仅标记，不包含逻辑）
 */
public enum GunFireType {
    // 单发：一次触发一发
    SEMI("SEMI"),
    // 全自动：按住持续射击，受RPM控制
    AUTO("AUTO"),
    // 三连发：一次触发连续射击固定次数
    BURST("BURST"),
    // 栓动：每次射击后需要循环/锁定动画
    BOLT("BOLT"),
    // 加特林：具备预热与加速射速机制的持续射击模式
    GATLING("GATLING");

    private final String id;

    GunFireType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static GunFireType getFireType(String name) {
        for (GunFireType type : values()) {
            if (type.id.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return SEMI;
    }

    public static GunFireType getFireType(int id) {
        for (GunFireType type : values()) {
            if (type.ordinal() == id) {
                return type;
            }
        }
        return SEMI;
    }
}