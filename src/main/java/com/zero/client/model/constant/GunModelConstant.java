package com.zero.client.model.constant;

/**
 * 枪械模型相关的骨骼 / 节点常量
 * <p>
 * 用于枪械模型动画系统中的固定挂点命名
 * 例如：枪口火焰、弹壳抛出、手部绑定、配件位置等
 */
public final class GunModelConstant {

    /**
     * 防止实例化
     */
    private GunModelConstant() {}

    public static final String IRON_VIEW = "iron_view"; // 机瞄定位
    public static final String MUZZLE_FLASH_ORIGIN = "muzzle_flash"; // 枪口火焰定位
    public static final String SHELL_ORIGIN = "shell"; // 弹壳抛出定位
    public static final String SHELL_ORIGIN_NODE_PREFIX = "shell_";
    public static final String LEFT_HAND_POS = "lefthand_pos"; // 左手位置
    public static final String RIGHT_HAND_POS = "righthand_pos"; // 右手位置

    public static final String BULLET_IN_BARREL = "bullet_in_barrel";//枪管中的子弹，用于闭膛待击枪械的渲染，枪管中没有子弹时隐藏该组
    public static final String BULLET_IN_MAG = "bullet_in_mag";//弹匣内的子弹，会在弹匣打空时隐藏该组
    public static final String BULLET_CHAIN = "bullet_chain";//弹链，多用于机枪，在子弹打空时隐藏

    public static final String MOUNT = "mount";//有瞄具时显示，用于放瞄具的导轨（如 AKM 的导轨）
    public static final String SIGHT = "sight";//无瞄具时可见，机械瞄具
    public static final String SIGHT_FOLDED = "sight_folded";//有瞄具时显示，折叠的机械瞄具
    public static final String CARRY = "carry";//无瞄具时可见，通常用于 M4 上

    public static final String MAG_EXTENDED_1 = "mag_extended_1"; // 扩容弹匣1
    public static final String MAG_EXTENDED_2 = "mag_extended_2"; // 扩容弹匣2
    public static final String MAG_EXTENDED_3 = "mag_extended_3"; // 扩容弹匣3
    public static final String MAG_STANDARD = "mag_standard"; // 标准弹匣

    public static final String MAG_NORMAL_NODE = "magazine";//弹匣定位组
    public static final String MAG_ADDITIONAL_NODE = "additional_magazine";//换弹时第二个弹匣定位组


    public static final String ATTACHMENT_ADAPTER_NODE = "attachment_adapter";//配件转接口
    public static final String HANDGUARD_DEFAULT_NODE = "handguard_default";//默认护木
    public static final String HANDGUARD_TACTICAL_NODE = "handguard_tactical";//战术护木

    public static final String REFIT_VIEW_NODE = "refit_view";//默认的改装界面定位组
    public static final String REFIT_VIEW_PREFIX = "refit_";//改装界面视角的定位组前缀，实际名称为：前缀 + 配件名（小写）+ 后缀
    public static final String REFIT_VIEW_SUFFIX = "_view";//改装界面视角的定位组后缀，实际名称为：前缀 + 配件名（小写）+ 后缀

    public static final String ATTACHMENT_POS_SUFFIX = "_pos"; // 配件定位组后缀，实际名称为配件名（小写）+ 此后缀
    public static final String DEFAULT_ATTACHMENT_SUFFIX = "_default"; // 默认配件组后缀（安装配件后隐藏），实际名称为配件名（小写）+ 此后缀
}