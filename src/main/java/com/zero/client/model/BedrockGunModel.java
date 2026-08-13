package com.zero.client.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zero.client.animation.json.BonePose;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.model.bedrock.BedrockPartWrapper;
import com.zero.client.model.display.TextShowText;
import com.zero.client.model.functional.*;
import com.zero.client.model.json.BedrockJson;
import com.zero.client.model.functional.BeamRenderer;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.render.GlZero;
import com.zero.server.item.ItemGun;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;

import static com.zero.client.model.constant.GunModelConstant.*;

public class BedrockGunModel extends BedrockAnimatedModel {
    protected final EnumMap<IAttachmentType, List<BedrockPart>> refitAttachmentViewPath = Maps.newEnumMap(IAttachmentType.class);
    private final EnumMap<IAttachmentType, ItemStack> currentAttachmentItem = Maps.newEnumMap(IAttachmentType.class);
    private final Set<String> adapterToRender = Sets.newHashSet();
    private final ArrayList<ShellRender> shellRenderList = new ArrayList<>();
    //机瞄定位
    protected @Nullable List<BedrockPart> ironSightPath;
    // 瞄具配件定位组的路径。
    protected @Nullable List<BedrockPart> scopePosPath;
    // 弹匣定位组
    protected @Nullable BedrockPart magazineNode;
    // 换弹时第二个弹匣定位组
    protected @Nullable BedrockPart additionalMagazineNode;
    //激光
    protected @Nullable List<BedrockPart> laserBeamPaths;

    private ItemStack currentGunItem;

    private int currentExtendMagLevel = 0;//扩容弹夹等级

    public BedrockGunModel(BedrockJson json) {
        super(json);
        magazineNode = getBedrockPart(MAG_NORMAL_NODE);
        additionalMagazineNode = getBedrockPart(MAG_ADDITIONAL_NODE);
        //机瞄定位
        ironSightPath = getPath(modelMap.get(IRON_VIEW));
        //瞄具
        scopePosPath = getPath(modelMap.get(IAttachmentType.SCOPE.getKey().toLowerCase() + ATTACHMENT_POS_SUFFIX));

        laserBeamPaths = getPath(modelMap.get("laser_beam"));

        //手臂定位组替换为原本的手臂
        this.setFunctionalRenderer(LEFT_HAND_POS, new LeftHandRender(this));
        this.setFunctionalRenderer(RIGHT_HAND_POS, new RightHandRender(this));
        this.setFunctionalRenderer(MUZZLE_FLASH_ORIGIN, new MuzzleFlashRender(this));
        this.flush();
        // 准备各个配件的渲染
        allAttachmentRender();
        // 缓存抛壳窗
        this.cacheShellOriginNodes();
        // 缓存改装 UI 下各个配件的特写视角定位组
        cacheRefitAttachmentViewPath();
    }

    private void flush() {
        // 枪管内的子弹，用于闭膛待机枪械
        ammoHiddenRender(BULLET_IN_BARREL);
        // 弹匣内子弹
        ammoHiddenRender(BULLET_IN_MAG);
        // 机枪弹链
        ammoHiddenRender(BULLET_CHAIN);
        // 有瞄具时显示，用于放瞄具的导轨（如 AKM 的导轨）
        scopeHiddenRender(MOUNT);
        // 无瞄具时可见，通常用于 M4 上
        scopeHiddenARender(CARRY);
        // 有瞄具时显示，折叠的机械瞄具
        scopeHiddenRender(SIGHT_FOLDED);
        // 无瞄具时可见，机械瞄具
        scopeHiddenARender(SIGHT);

        // 安装一级扩容弹匣时显示
        extendedMagHiddenRender(MAG_EXTENDED_1, 1);
        // 安装二级扩容弹匣时显示
        extendedMagHiddenRender(MAG_EXTENDED_2, 2);
        // 安装三级扩容弹匣时显示
        extendedMagHiddenRender(MAG_EXTENDED_3, 3);
        // 没有安装扩容弹匣时显示
        extendedMagHiddenRender(MAG_STANDARD, 0);

        // 部分枪械换弹动画播放时，会同时出现两个弹匣，这个就是程序自动渲染另一个弹匣的代码
        this.setFunctionalRenderer(MAG_ADDITIONAL_NODE, renderAdditionalMagazine(MAG_ADDITIONAL_NODE));
        // 默认护木渲染
        handguardDefaultRender(HANDGUARD_DEFAULT_NODE);
        // 战术护木渲染
        handguardTacticalRender(HANDGUARD_TACTICAL_NODE);

        // 配件转接口渲染
        attachmentAdapterNodeRender(ATTACHMENT_ADAPTER_NODE);
        updateAttachmentVisibility();
    }

    private void cacheShellOriginNodes() {
        BedrockPartWrapper rendererWrapper = getBedrockPartWrapper(SHELL_ORIGIN);
        int i = 1;
        while (rendererWrapper != null) {
            ShellRender shellRender = new ShellRender(this);
            this.setFunctionalRenderer(rendererWrapper.getModelRenderer().name, shellRender);
            shellRenderList.add(shellRender);
            rendererWrapper = modelMap.get(SHELL_ORIGIN_NODE_PREFIX + i);
            i++;
        }
    }

    // 缓存改装 UI 下各个配件的特写视角定位组
    private void cacheRefitAttachmentViewPath() {
        for (IAttachmentType type : IAttachmentType.values()) {
            if (type == IAttachmentType.NONE) {
                refitAttachmentViewPath.put(type, getPath(modelMap.get(REFIT_VIEW_NODE)));
                continue;
            }
            String nodeName = REFIT_VIEW_PREFIX + type.name().toLowerCase() + REFIT_VIEW_SUFFIX;
            refitAttachmentViewPath.put(type, getPath(modelMap.get(nodeName)));
        }
    }

    //渲染全部配件
    private void allAttachmentRender() {
        for (IAttachmentType type : IAttachmentType.values()) {
            if (type == IAttachmentType.NONE || type == IAttachmentType.SCOPE) {
                continue;
            }
            String positionNodeName = type.getKey().toLowerCase() + ATTACHMENT_POS_SUFFIX;

            BedrockPartWrapper position = getBedrockPartWrapper(positionNodeName);

            IFunctionalRenderer positionRenderer = null;
            if (position != null) {
                position.getModelRenderer().visible = false;
                positionRenderer = new AttachmentRender(this, type);
            }
            this.setFunctionalRenderer(positionNodeName, positionRenderer);
        }
    }

    private void updateAttachmentVisibility() {
        for (IAttachmentType type : IAttachmentType.values()) {
            if (type == IAttachmentType.NONE || type == IAttachmentType.SCOPE) {
                continue;
            }
            ItemStack attachmentItem = currentAttachmentItem.get(type);

            String defaultNodeName = type.getKey().toLowerCase() + DEFAULT_ATTACHMENT_SUFFIX;

            BedrockPartWrapper defaultPart = getBedrockPartWrapper(defaultNodeName);

            if (defaultPart != null) {
                if (type == IAttachmentType.MUZZLE && checkShowMuzzle(defaultPart.getModelRenderer(), attachmentItem)) {
                    continue;
                }
                defaultPart.getModelRenderer().visible = attachmentItem == null || attachmentItem.isEmpty();
            }
        }
    }

    private static boolean checkShowMuzzle(BedrockPart bedrockPart, ItemStack attachmentItem) {
        AttachmentType iAttachment = AttachmentType.getFromItemStack(attachmentItem);
        if (iAttachment != null) {
            bedrockPart.visible = iAttachment.muzzleFlash;
            return true;
        }
        return false;
    }


    public void render(CustomItemRenderType customItemRenderType, ItemStack stack, GunType gunType) {
        currentExtendMagLevel = 0;
        adapterToRender.clear();
        currentGunItem = stack;
        for (IAttachmentType type : IAttachmentType.values()) {
            if (type == IAttachmentType.NONE) {
                continue;
            }
            ItemStack attachmentItem = gunType.getAttachmentItemStack(stack, type);
            if (attachmentItem.isEmpty()) {

            }
            currentAttachmentItem.put(type, attachmentItem);
            AttachmentType attachment = AttachmentType.getFromItemStack(attachmentItem);
            if (attachment != null) {
                // 读取扩容等级，为扩容弹匣渲染做准备
                if (type == IAttachmentType.EXTENDED_MAG) {
                    currentExtendMagLevel = attachment.level;
                }
                if (!(attachment.adapter == null) && !attachment.adapter.equalsIgnoreCase("")) {
                    adapterToRender.add(attachment.adapter);
                }
                BedrockAttachmentModel attachmentModel = attachment.getAnimatedModel();
                if (attachmentModel != null) {
                    attachmentModel.setItemStack(attachmentItem);
                }
            }
        }
        this.flush();
        if (gunType.textShowText != null) {
            setTextShowList(gunType.textShowText,currentGunItem);
        }
        if (laserBeamPaths != null) {
            BeamRenderer.renderLaserBeam(stack, laserBeamPaths);
        }
        // 镜子需要先渲染，写入模板值
        ItemStack attachmentItem = currentAttachmentItem.get(IAttachmentType.SCOPE);
        AttachmentType attachment = AttachmentType.getFromItemStack(attachmentItem);
        if (scopePosPath != null && attachmentItem != null && !attachmentItem.isEmpty()) {
            GlStateManager.pushMatrix();
            for (BedrockPart part : scopePosPath) {
                part.applyTranslateAndRotate();
            }
            AttachmentRender.renderAttachment(attachmentItem, stack, customItemRenderType);
            GlStateManager.popMatrix();
            if (attachment != null) {
                if (attachment.isScope) {
                    GlZero.enableItemEntityStencilTest();
                }
            }
        }
        GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        super.render(customItemRenderType);
        GlZero.disableItemEntityStencilTest();
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
    }

    @Nullable
    private IFunctionalRenderer renderAdditionalMagazine(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper == null) {
            return null;
        }
        BedrockPart bedrockPart = partWrapper.getModelRenderer();
        return type -> {
            if (bedrockPart.visible) {
                bedrockPart.compile();
                for (BedrockPart part : bedrockPart.children) {
                    part.render(type);
                }
                if (magazineNode != null && magazineNode.visible) {
                    magazineNode.compile();
                    for (BedrockPart part : magazineNode.children) {
                        part.render(type);
                    }
                }
            }
        };
    }

    private void ammoHiddenRender(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null && currentGunItem != null) {
            BedrockPart bedrockPart = partWrapper.getModelRenderer();
            GunType gunType = GunType.getGunType(currentGunItem);
            if (gunType != null) {
                ItemGun gun = (ItemGun) currentGunItem.getItem();
                bedrockPart.visible = gun.getCurrentAmmo(currentGunItem) > 0;
            }
        }
    }


    private void attachmentAdapterNodeRender(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null) {
            for (BedrockPart child : partWrapper.getModelRenderer().children) {
                if (child.name == null) {
                    child.visible = false;
                    continue;
                }
                child.visible = adapterToRender.contains(child.name);
            }
        }
    }

    private void scopeHiddenRender(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null) {
            // 安装瞄具时可见
            ItemStack scopeItem = currentAttachmentItem.get(IAttachmentType.SCOPE);
            partWrapper.getModelRenderer().visible = scopeItem != null && !scopeItem.isEmpty();
        }
    }

    private void scopeHiddenARender(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null) {
            // 安装瞄具时可见
            ItemStack scopeItem = currentAttachmentItem.get(IAttachmentType.SCOPE);
            partWrapper.getModelRenderer().visible = scopeItem == null || scopeItem.isEmpty();
        }
    }

    private void extendedMagHiddenRender(String node, int level) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null) {
            partWrapper.getModelRenderer().visible = currentExtendMagLevel == level;
        }
    }

    private void handguardTacticalRender(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null) {
            ItemStack laserItem = currentAttachmentItem.get(IAttachmentType.LASER);
            ItemStack gripItem = currentAttachmentItem.get(IAttachmentType.GRIP);
            partWrapper.getModelRenderer().visible = (laserItem != null && !laserItem.isEmpty()) || (gripItem != null && !gripItem.isEmpty());
        }
    }

    private void handguardDefaultRender(String node) {
        BedrockPartWrapper partWrapper = getBedrockPartWrapper(node);
        if (partWrapper != null) {
            ItemStack laserItem = currentAttachmentItem.get(IAttachmentType.LASER);
            ItemStack gripItem = currentAttachmentItem.get(IAttachmentType.GRIP);
            partWrapper.getModelRenderer().visible = (laserItem != null && !laserItem.isEmpty()) || (gripItem != null && !gripItem.isEmpty());
        }
    }

    @Override
    public void applyAnimation(Map<String, BonePose> poseMap) {
        if (poseMap.containsKey(MAG_ADDITIONAL_NODE)) {
            if (additionalMagazineNode != null) {
                additionalMagazineNode.visible = true;
            }
        }
        super.applyAnimation(poseMap);
    }

    @Override
    public void cleanAnimationTransform() {
        super.cleanAnimationTransform();
        if (additionalMagazineNode != null) {
            additionalMagazineNode.visible = false;
        }
    }

    public EnumMap<IAttachmentType, ItemStack> getCurrentAttachmentItem() {
        return currentAttachmentItem;
    }

    public ItemStack getCurrentGunItem() {
        return currentGunItem;
    }

    @Nullable
    public List<BedrockPart> getRefitAttachmentViewPath(IAttachmentType type) {
        return refitAttachmentViewPath.get(type);
    }

    @Nullable
    public ShellRender getShellRender(int index) {
        if (index < 0 || index >= shellRenderList.size()) {
            return null;
        }
        return shellRenderList.get(index);
    }

    @Nullable
    public List<BedrockPart> getIronSightPath() {
        return ironSightPath;
    }

    @Nullable
    public List<BedrockPart> getScopePosPath() {
        return scopePosPath;
    }
}
