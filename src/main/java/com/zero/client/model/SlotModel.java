package com.zero.client.model;

import com.zero.client.model.bedrock.BedrockCubePerFace;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.model.util.FaceUVsItem;
import com.zero.client.render.CustomItemRenderType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

public class SlotModel{
    private final BedrockPart bone;

    public SlotModel(boolean illuminated) {
        bone = new BedrockPart("slot");
        bone.setPos(8.0F, 24.0F, -10.0F);
        bone.cubes.add(new BedrockCubePerFace(-16.0F, -16.0F, 9.5F, 16.0F, 16.0F, 0, 0, 16, 16, FaceUVsItem.singleSouthFace()));
        bone.illuminated = illuminated;
    }

    public SlotModel() {
        this(false);
    }

    public void render() {
        bone.render(CustomItemRenderType.FRAME);
    }
}
