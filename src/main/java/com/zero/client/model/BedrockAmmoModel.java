package com.zero.client.model;

import com.zero.client.model.bedrock.BedrockModel;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.model.json.BedrockJson;

import javax.annotation.Nullable;
import java.util.List;

public class BedrockAmmoModel extends BedrockModel {
    private static final String FIXED_ORIGIN_NODE = "fixed";
    private static final String GROUND_ORIGIN_NODE = "ground";
    private static final String THIRD_PERSON_HAND_ORIGIN_NODE = "thirdperson_hand";

    // 展示框渲染原点定位组的路径
    protected @Nullable List<BedrockPart> fixedOriginPath;
    // 地面实体渲染原点定位组的路径
    protected @Nullable List<BedrockPart> groundOriginPath;
    // 第三人称手部实体渲染原点定位组的路径
    protected @Nullable List<BedrockPart> thirdPersonHandOriginPath;

    public BedrockAmmoModel(BedrockJson json) {
        super(json);
        fixedOriginPath = getPath(modelMap.get(FIXED_ORIGIN_NODE));
        groundOriginPath = getPath(modelMap.get(GROUND_ORIGIN_NODE));
        thirdPersonHandOriginPath = getPath(modelMap.get(THIRD_PERSON_HAND_ORIGIN_NODE));
    }

    @Nullable
    public List<BedrockPart> getFixedOriginPath() {
        return fixedOriginPath;
    }

    @Nullable
    public List<BedrockPart> getGroundOriginPath() {
        return groundOriginPath;
    }

    @Nullable
    public List<BedrockPart> getThirdPersonHandOriginPath() {
        return thirdPersonHandOriginPath;
    }
}
