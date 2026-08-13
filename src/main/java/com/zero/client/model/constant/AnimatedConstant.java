package com.zero.client.model.constant;

/**
 * 动画系统中使用的固定节点名称常量
 * <p>
 * 用于替代 enum，改为纯字符串常量，提高运行效率并简化访问方式
 * 适用于：骨骼系统 / 动画节点 / 渲染定位 / 相机控制
 */
public final class AnimatedConstant {

    /**
     * 防止实例化
     */
    private AnimatedConstant() {}

    /**
     * 相机节点
     */
    public static final String CAMERA = "camera";

    /**
     * 约束节点（用于动画约束 / IK / 父子绑定控制）
     */
    public static final String CONSTRAINT_NODE = "constraint";

    /**
     * 根节点（所有骨骼或动画的起点）
     */
    public static final String ROOT = "root";

    /**
     * 第一人称视角模型定位（用于手部/武器/视角绑定）
     */
    public static final String IDLE_VIEW = "idle_view";

    /**
     * 第三人称手部模型原点（玩家外部视角手部定位）
     */
    public static final String THIRD_PERSON_HAND_ORIGIN = "thirdperson_hand";

    /**
     * 展示框 / ItemFrame / GUI展示模型定位
     */
    public static final String FIXED_ORIGIN = "fixed";

    /**
     * 实体在地面或世界中的标准渲染定位
     */
    public static final String ENTITY = "ground";
}