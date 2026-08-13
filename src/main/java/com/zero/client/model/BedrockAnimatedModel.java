package com.zero.client.model;

import com.zero.client.animation.json.BonePose;
import com.zero.client.model.bedrock.BedrockModel;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.model.bedrock.BedrockPartWrapper;
import com.zero.client.model.display.TextShowText;
import com.zero.client.model.functional.FunctionalBedrockPart;
import com.zero.client.model.functional.IFunctionalRenderer;
import com.zero.client.model.functional.TextShowRender;
import com.zero.client.model.json.BedrockJson;
import com.zero.client.model.json.Bone;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;


import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.zero.client.model.constant.AnimatedConstant.*;

public class BedrockAnimatedModel extends BedrockModel {
    //摄像机动画
    public CameraAnimated cameraAnimated = new CameraAnimated();
    //ICA约束组
    protected @Nullable List<BedrockPart> constraintPath;
    private @Nullable ConstraintObject constraintObject;

    public BedrockPart root;//根

    public List<BedrockPart> idle_view;//第一人称的非瞄准视角

    public List<BedrockPart> Third_Person_Hand;//第三人称的手臂定位点

    public List<BedrockPart> Fixed;//世界展示框定位

    public List<BedrockPart> Entity;//世界掉落物定位

    private boolean renderHand = true;

    private ResourceLocation currentTexture;


    public BedrockAnimatedModel(BedrockJson json) {
        super(json);
        // 初始化相机动画对象
        BedrockPartWrapper cameraRendererWrapper = modelMap.get(CAMERA);
        if (cameraRendererWrapper != null) {
            cameraAnimated.cameraRenderer = cameraRendererWrapper;
        }
        // 初始化动画约束对象
        constraintPath = getPath(modelMap.get(CONSTRAINT_NODE));
        if (constraintPath != null) {
            constraintObject = new ConstraintObject();
        }
        root = getBedrockPart(ROOT);
        idle_view = getPath(getBedrockPartWrapper(IDLE_VIEW));
        Third_Person_Hand = getPath(getBedrockPartWrapper(THIRD_PERSON_HAND_ORIGIN));
        Fixed = getPath(getBedrockPartWrapper(FIXED_ORIGIN));
        Entity = getPath(getBedrockPartWrapper(ENTITY));
    }

    @Override
    public void loadModel(BedrockJson json) {
        // --- 建立索引：先创建所有 Part（空壳），便于后面 bind parent/child ---
        for (Bone b : json.bones) {
            modelMap.putIfAbsent(b.name, new BedrockPartWrapper(new FunctionalBedrockPart(null, b.name)));
        }
        super.loadModel(json);
    }

    public void cleanAnimationTransform() {
        for (BedrockPartWrapper rendererWrapper : modelMap.values()) {
            rendererWrapper.setOffsetX(0);
            rendererWrapper.setOffsetY(0);
            rendererWrapper.setOffsetZ(0);
            rendererWrapper.getAdditionalQuaternion().set(0, 0, 0, 1);
            rendererWrapper.setScaleX(1);
            rendererWrapper.setScaleY(1);
            rendererWrapper.setScaleZ(1);
        }
        if (constraintObject != null) {
            constraintObject.rotationConstraint.set(0, 0, 0);
            constraintObject.translationConstraint.set(0, 0, 0);
        }
    }

    public void cleanCameraAnimationTransform() {
        cameraAnimated.rotationQuaternion.set(0, 0, 0, 1);
    }

    public CameraAnimated getCameraAnimated() {
        return cameraAnimated;
    }

    /**
     * 应用 BonePose 到指定模型节点
     */
    public void applyBonePose(String nodeName, BonePose pose) {
        BedrockPartWrapper wrapper = modelMap.get(nodeName);
        if (wrapper == null) return;
        // 设置位置偏移
        if (pose.position != null) {
            wrapper.setOffsetX(pose.position[0] / 16f);
            wrapper.setOffsetY(-pose.position[1] / 16f);
            wrapper.setOffsetZ(pose.position[2] / 16f);
        }
        if (pose.scale != null) {
            // 设置缩放
            wrapper.setScaleX(pose.scale[0]);
            wrapper.setScaleY(pose.scale[1]);
            wrapper.setScaleZ(pose.scale[2]);
        }
        if (pose.rotation != null) {
            wrapper.getAdditionalQuaternion().set(pose.rotation[0], pose.rotation[1], pose.rotation[2], pose.rotation[3]);
        }
    }

    /**
     * 批量应用动画到模型
     */
    public void applyAnimation(Map<String, BonePose> poseMap) {
        for (Map.Entry<String, BonePose> entry : poseMap.entrySet()) {
            String nodeName = entry.getKey();
            BonePose bonePose = entry.getValue();

            if (CAMERA.equals(nodeName)) {
                cameraAnimated.updateFromBonePose(bonePose);
            } else if (CONSTRAINT_NODE.equals(nodeName) && constraintObject != null) {
                constraintObject.updateFromBonePose(bonePose);
            } else {
                applyBonePose(nodeName, bonePose);
            }
        }
    }

    @Nullable
    public ConstraintObject getConstraintObject() {
        return constraintObject;
    }

    @Nullable
    public List<BedrockPart> getConstraintPath() {
        return constraintPath;
    }

    /**
     * 添加枪械自定义的文本显示
     */
    public void setTextShowList(Map<String, TextShowText> textShowList, ItemStack stack) {
        textShowList.forEach((name, textShowText) -> this.setFunctionalRenderer(name, new TextShowRender(this, textShowText, stack)));
    }

    /**
     * @param node     部件名称
     * @param function FunctionalBedrockPart， 外挂渲染
     */
    public void setFunctionalRenderer(String node, IFunctionalRenderer function) {
        BedrockPartWrapper bedrockPart = modelMap.get(node);
        if (bedrockPart == null) {
            modelMap.put(node, new BedrockPartWrapper(new FunctionalBedrockPart(function, node)));
        } else if (bedrockPart.getModelRenderer() instanceof FunctionalBedrockPart) {
            FunctionalBedrockPart functionalBedrockPart = (FunctionalBedrockPart) bedrockPart.getModelRenderer();
            functionalBedrockPart.function = function;
        }
    }


    public BedrockPart getRoot() {
        return root;
    }

    public List<BedrockPart> getIdle_view() {
        return idle_view;
    }

    public List<BedrockPart> getThird_Person_Hand() {
        return Third_Person_Hand;
    }

    public List<BedrockPart> getFixed() {
        return Fixed;
    }

    public List<BedrockPart> getEntity() {
        return Entity;
    }

    public void setCurrentTexture(ResourceLocation currentTexture) {
        this.currentTexture = currentTexture;
    }

    public ResourceLocation getCurrentTexture() {
        return currentTexture;
    }

    public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }

    public boolean getRenderHand() {
        return renderHand;
    }


    public void applyNodePathInverse(List<BedrockPart> nodePath, float[] scale) {
        if (nodePath == null || nodePath.isEmpty()) return;

        float sx = (scale != null) ? scale[0] : 1f;
        float sy = (scale != null) ? scale[1] : 1f;
        float sz = (scale != null) ? scale[2] : 1f;

        for (int i = nodePath.size() - 1; i >= 0; i--) {

            BedrockPart part = nodePath.get(i);

            // ===== rotation inverse =====
            if (part.xRot != 0)
                GlStateManager.rotate(-part.xRot * 57.295776f, 1, 0, 0);
            if (part.yRot != 0)
                GlStateManager.rotate(-part.yRot * 57.295776f, 0, 1, 0);
            if (part.zRot != 0)
                GlStateManager.rotate(-part.zRot * 57.295776f, 0, 0, 1);

            if (part.getParent() != null) {
                GlStateManager.translate(-part.x * sx / 16f, -part.y * sy / 16f, -part.z * sz / 16f);
            } else {
                GlStateManager.translate(-part.x * sx / 16f, (1.5 - part.y / 16) * sy, -part.z * sz / 16f);
            }
        }
    }


}
