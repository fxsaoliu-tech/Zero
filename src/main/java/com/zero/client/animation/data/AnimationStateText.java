package com.zero.client.animation.data;

/**
 * 动画状态名称文本
 * 用于在控制器或状态机中统一引用动画名称
 */
public class AnimationStateText {
    // 静态 / 闲置动画
    public static final String STATIC_IDLE = "static_idle";
    // 呼吸
    public static final String IDLE_BREATHE = "idle_breathe";
    // 动作类动画
    public static final String DRAW = "draw";
    public static final String PUT_AWAY = "put_away";
    // Walk 三方向
    public static final String WALK_FORWARD = "walk_forward";
    public static final String WALK_BACKWARD = "walk_backward";
    public static final String WALK_SIDE = "walk_sideway";
    // Sprint 四阶段
    public static final String SPRINT_IN = "run_start";
    public static final String SPRINT_LOOP = "run";
    public static final String SPRINT_LOCK = "run_hold";
    public static final String SPRINT_OUT = "run_end";
    //检视
    public static final String INSPECT = "inspect";


    //枪械使用的
    public static final String INSPECT_EMPTY = "inspect_empty";
    public static final String RELOAD_EMPTY = "reload_empty";
    public static final String RELOAD_TACTICAL = "reload_tactical";

    public static final String SHOOT = "shoot";

    public static final String BOLT = "bolt";

    public static final String SEMI = "switch_semi";
    public static final String AUTO = "switch_auto";
    public static final String BURST = "switch_burst";
}