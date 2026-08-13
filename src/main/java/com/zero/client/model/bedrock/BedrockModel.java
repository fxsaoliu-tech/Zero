package com.zero.client.model.bedrock;

import com.zero.client.model.functional.IFunctionalRenderer;
import com.zero.client.model.json.BedrockJson;
import com.zero.client.model.json.Bone;
import com.zero.client.model.json.Cube;
import com.zero.client.model.json.Description;
import com.zero.client.model.util.FaceItem;
import com.zero.client.render.CustomItemRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.*;

public class BedrockModel {
    public final Map<String, BedrockPartWrapper> modelMap = new LinkedHashMap<>(); // name -> part
    protected final Map<String, Bone> indexBones = new HashMap<>();            // name -> Bone 原始数据
    protected final List<BedrockPart> shouldRender = new ArrayList<>();       // 根 Part 列表（需要渲染的根）

    /**
     * 委托到渲染结束时执行的渲染器，用于特殊部分的渲染，如手臂
     */
    protected List<IFunctionalRenderer> delegateRenderers = new ArrayList<>();

    public Description description;
    public float[] visibleBoundsOffset = null;

    public BedrockModel(BedrockJson json) {
        if (json == null) throw new IllegalArgumentException("BedrockJson is null");
        loadModel(json);
        // 应用发光
        for (BedrockPartWrapper bedrockPart : modelMap.values()) {
            if (bedrockPart.getModelRenderer().name != null && bedrockPart.getModelRenderer().name.endsWith("_illuminated")) {
                bedrockPart.getModelRenderer().illuminated = true;
            }
        }
    }

    public void loadModel(BedrockJson json) {
        this.description = json.description;

        // --- 建立索引 ---
        for (Bone b : json.bones) {
            indexBones.put(b.name, b);
            modelMap.putIfAbsent(b.name, new BedrockPartWrapper(new BedrockPart(b.name)));
        }
        // --- 填充 Part 的 pivot / rotation / cubes ---
        for (Bone b : json.bones) {
            BedrockPart part = modelMap.get(b.name).getModelRenderer();
            // 1) 设置 bone 的 pivot
            if (b.pivot != null && b.pivot.size() >= 3) {
                float pivotX = getBonePivotX(b);
                float pivotY = getBonePivotY(b);
                float pivotZ = getBonePivotZ(b);
                part.setPos(pivotX, pivotY, pivotZ);
            }
            // 2) 设置 bone 的初始旋转
            if (b.rotation != null && b.rotation.size() >= 3) {
                part.xRot = degToRad(b.rotation.get(0));
                part.yRot = degToRad(b.rotation.get(1));
                part.zRot = degToRad(b.rotation.get(2));
                part.setRotate(part.xRot, part.yRot, part.zRot);
            }
            // 3) 处理 bone 下的 cubes
            if (b.cubes == null) {
                continue;
            }
            int cubeIndex = 0;
            for (Cube c : b.cubes) {
                float inflate = c.inflate;
                boolean mirror = c.mirror;

                // 3A) 带 rotation 的 cube -> 新建子 Part
                if (c.rotation != null && c.rotation.size() >= 3 && c.pivot != null && c.pivot.size() >= 3) {
                    String cubePartName = b.name + "_cube_" + cubeIndex++;
                    BedrockPart cubePart = new BedrockPart(cubePartName);
                    // 取得 cubePivot（如果cube没有pivot，使用bone的pivot）
                    float cubePivotX = c.pivot.get(0);
                    float cubePivotY = c.pivot.get(1);
                    float cubePivotZ = c.pivot.get(2);
                    // 计算子 Part 的 rotationPoint（相对于父骨骼）
                    // 规则：rp = cubePivot - bonePivot
                    // Y轴需要翻转：基岩版Y向上，Java版Y向下
                    float rpX = cubePivotX - b.pivot.get(0);
                    float rpY = b.pivot.get(1) - cubePivotY; // 父 - 子
                    float rpZ = cubePivotZ - b.pivot.get(2);
                    cubePart.setPos(rpX, rpY, rpZ);
                    // 设置子 Part 的旋转
                    cubePart.xRot = degToRad(c.rotation.get(0));
                    cubePart.yRot = degToRad(c.rotation.get(1));
                    cubePart.zRot = degToRad(c.rotation.get(2));
                    cubePart.setRotate(cubePart.xRot, cubePart.yRot, cubePart.zRot);

                    // 子 Part 内部的 ModelBox origin（相对于cubePivot）
                    // 规则：localOrigin = origin - cubePivot
                    // Y轴需要额外减去size.y（因为原点位置不同）
                    float localOriginX = c.origin.get(0) - cubePivotX;
                    float localOriginY = cubePivotY - c.origin.get(1) - c.size.get(1); // 关键修正
                    float localOriginZ = c.origin.get(2) - cubePivotZ;
                    // 创建 cube
                    if (c.isBox) {
                        FaceItem fi = (c.uv != null && c.uv.north != null) ? c.uv.north : new FaceItem(new float[]{0f, 0f}, new float[]{1f, 1f});
                        cubePart.cubes.add(new BedrockCubeBox(fi.getUv()[0], fi.getUv()[1], localOriginX, localOriginY, localOriginZ, c.size.get(0), c.size.get(1), c.size.get(2), inflate, mirror, description != null ? description.texture_width : 64f, description != null ? description.texture_height : 32f));
                    } else {
                        cubePart.cubes.add(new BedrockCubePerFace(localOriginX, localOriginY, localOriginZ, c.size.get(0), c.size.get(1), c.size.get(2), inflate, description != null ? description.texture_width : 64f, description != null ? description.texture_height : 32f, c.uv));
                    }
                    // 把 cubePart 作为子节点挂到当前 bone 的 Part 下
                    part.addChild(cubePart);
                }
                // 3B) 普通 cube（无 rotation） -> 直接加入到 bone 对应的 Part.cubes
                else {
                    // 普通 cube 的 ModelBox origin（相对于bone的pivot）
                    // 规则：boxOrigin = origin - bonePivot
                    // Y轴需要额外减去size.y并翻转
                    float boxX = c.origin.get(0) - b.pivot.get(0);
                    float boxY = b.pivot.get(1) - c.origin.get(1) - c.size.get(1); // 父 - 原点 - 尺寸
                    float boxZ = c.origin.get(2) - b.pivot.get(2);

                    if (c.isBox) {
                        FaceItem fi = (c.uv != null && c.uv.north != null) ? c.uv.north : new FaceItem(new float[]{0f, 0f}, new float[]{1f, 1f});
                        part.cubes.add(new BedrockCubeBox(fi.getUv()[0], fi.getUv()[1], boxX, boxY, boxZ, c.size.get(0), c.size.get(1), c.size.get(2), inflate, c.mirror, description != null ? description.texture_width : 64f, description != null ? description.texture_height : 32f));
                    } else {
                        part.cubes.add(new BedrockCubePerFace(boxX, boxY, boxZ, c.size.get(0), c.size.get(1), c.size.get(2), inflate, description != null ? description.texture_width : 64f, description != null ? description.texture_height : 32f, c.uv));
                    }
                    cubeIndex++;
                }
            }
        }
        // --- 绑定 parent-child（Bone 层面的父子关系） ---
        for (Bone b : json.bones) {
            BedrockPart part = modelMap.get(b.name).getModelRenderer();
            if (b.parent != null && modelMap.containsKey(b.parent)) {
                BedrockPart parent = modelMap.get(b.parent).getModelRenderer();
                parent.addChild(part);
                part.parent = parent;
            } else {
                // 根节点
                shouldRender.add(part);
                part.parent = null;
            }
        }
        // visible_bounds_offset（如果存在）
        if (description != null && description.visible_bounds_offset != null && description.visible_bounds_offset.size() >= 3) {
            visibleBoundsOffset = new float[]{description.visible_bounds_offset.get(0), description.visible_bounds_offset.get(1), description.visible_bounds_offset.get(2)};
        }
    }

    private float degToRad(float deg) {
        return (float) Math.toRadians(deg);
    }

    /**
     * 计算 Bone 对应的 ModelRenderer.setRotationPoint 的 X 分量
     * - 子骨骼: child.x - parent.x
     * - 根骨骼: child.x
     */
    private float getBonePivotX(Bone b) {
        if (b.parent != null && indexBones.containsKey(b.parent))
            return b.pivot.get(0) - indexBones.get(b.parent).pivot.get(0);
        return b.pivot.get(0);
    }

    /**
     * 计算 Bone 对应的 ModelRenderer.setRotationPoint 的 Y 分量
     * - 子骨骼: parent.y - child.y  （因为Y轴方向相反）
     * - 根骨骼: 24 - child.y （基准高度调整）
     */
    private float getBonePivotY(Bone b) {
        if (b.parent != null && indexBones.containsKey(b.parent))
            return indexBones.get(b.parent).pivot.get(1) - b.pivot.get(1);
        return 24f - b.pivot.get(1);
    }

    /**
     * 计算 Bone 对应的 ModelRenderer.setRotationPoint 的 Z 分量
     * - 子骨骼: child.z - parent.z
     * - 根骨骼: child.z
     */
    private float getBonePivotZ(Bone b) {
        if (b.parent != null && indexBones.containsKey(b.parent))
            return b.pivot.get(2) - indexBones.get(b.parent).pivot.get(2);
        return b.pivot.get(2);
    }

    // ------------------- 渲染委托 -------------------

    /**
     * 将渲染请求委托给根 Part（Part 会递归渲染子节点）
     */
    public void render(CustomItemRenderType type) {
        GlStateManager.pushMatrix();
        for (BedrockPart part : shouldRender) {
            part.render(type);
        }
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        for (IFunctionalRenderer renderer : delegateRenderers) {
            renderer.render(type);
        }
        GlStateManager.popMatrix();
        delegateRenderers = new ArrayList<>();
    }

    protected List<BedrockPart> getPath(@Nullable BedrockPartWrapper rendererWrapper) {
        if (rendererWrapper == null) {
            return null;
        }
        BedrockPart part = rendererWrapper.getModelRenderer();
        List<BedrockPart> path = new ArrayList<>();
        Stack<BedrockPart> stack = new Stack<>();
        do {
            stack.push(part);
            part = part.getParent();
        } while (part != null);
        while (!stack.isEmpty()) {
            part = stack.pop();
            path.add(part);
        }
        return path;
    }

    public BedrockPartWrapper getBedrockPartWrapper(String id) {
        if (modelMap.containsKey(id)) {
            return modelMap.get(id);
        }
        return null;
    }

    public BedrockPart getBedrockPart(String id) {
        if (modelMap.containsKey(id)) {
            return modelMap.get(id).getModelRenderer();
        }
        return null;
    }

    public void delegateRender(IFunctionalRenderer renderer) {
        delegateRenderers.add(renderer);
    }
}