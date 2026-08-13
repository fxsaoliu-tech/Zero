package com.zero.server.type;

import com.zero.Zero;
import com.zero.client.model.BedrockAttachmentModel;
import com.zero.client.model.BedrockGunModel;
import com.zero.server.file.FileList;
import com.zero.server.item.ItemAttachment;
import com.zero.server.type.mode.IAttachmentType;
import com.zero.server.type.tab.EnumTabType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class AttachmentType extends InfoType implements IScope {
    // 后坐力 (枪口,握把,枪托共用)
    public float recoilVerticalMultiplier = 0;//垂直
    public float recoilHorizontalMultiplier = 0;//水平
    //枪口
    public float spreadMultiplier = 0;// 散布
    public boolean silencer = false;// 消音
    public boolean muzzleFlash = true; // 枪口火焰
    //瞄准镜
    public float viewFov = 70f;
    public float fovZoomLevel = 1.5f;
    public float aimSpeed = 0.25f;
    public boolean isScope = false;
    //扩容弹夹
    public int level = 1;
    //配件转接口
    public String adapter = "";

    //模型
    @SideOnly(Side.CLIENT)
    private BedrockAttachmentModel bedrockAttachmentModel;
    //lod
    @SideOnly(Side.CLIENT)
    public BedrockAttachmentModel attachmentModelLod;
    //贴图
    public String textureLod;

    public static Map<String, AttachmentType> attachments = new HashMap<>();

    //配置属性
    public IAttachmentType enumAttachmentType = IAttachmentType.NONE;

    public AttachmentType() {

    }

    @Override
    public AttachmentType loadContent(FileList fileList) {
        super.loadContent(fileList);
        return this;
    }


    @Override
    protected void read(String[] content, FileList file) {
        super.read(content, file);
        // (枪口，握把，枪托共用)后坐力
        recoilVerticalMultiplier = Read(content, "recoilVerticalMultiplier", recoilVerticalMultiplier, file);
        recoilHorizontalMultiplier = Read(content, "recoilHorizontalMultiplier", recoilHorizontalMultiplier, file);
        //枪口属性
        spreadMultiplier = Read(content, "spreadMultiplier", spreadMultiplier, file);
        silencer = Read(content, "silencer", silencer, file);
        muzzleFlash = Read(content, "muzzleFlashText", muzzleFlash, file);
        // 瞄准镜
        viewFov = Read(content, "viewFov", viewFov, file);
        fovZoomLevel = Read(content, "fovZoomLevel", fovZoomLevel, file);
        aimSpeed = Read(content, "aimSpeed", aimSpeed, file);
        isScope = Read(content, "isScope", isScope, file);
        //扩容弹夹
        level = Read(content, "level", level, file);
        // 配件类型
        String type = Read(content, "type", "", file);
        if (!type.equalsIgnoreCase("")) {
            enumAttachmentType = IAttachmentType.getAttachmentType(type.toUpperCase());
            enumTabType = EnumTabType.getEnumInfoType(type.toUpperCase());
        }
    }

    @Override
    protected void readClient(String[] content, FileList file) {
        super.readClient(content, file);
        String model = Read(content, "model", "", file);
        textureLod = Read(content, "textureLod", textureLod, file);
        String modelL = Read(content, "modelLod", "", file);

        if (!model.isEmpty()) {
            bedrockAttachmentModel = new BedrockAttachmentModel(Zero.server.loadModel(file, "attachment/" + model));
        }
        if (!modelL.isEmpty()) {
            this.attachmentModelLod = new BedrockAttachmentModel(Zero.server.loadModel(file, "attachment/lod/" + modelL));
        }
        if (enumAttachmentType == IAttachmentType.SCOPE && bedrockAttachmentModel != null) {
            bedrockAttachmentModel.setIsScope(isScope);
        }
        adapter = Read(content, "adapter", adapter, file);
    }

    @Override
    protected void endRead(FileList file) {
        super.endRead(file);
        attachments.put(id, this);
    }


    public static AttachmentType getFromNBT(NBTTagCompound tags) {
        ItemStack stack = new ItemStack(tags);
        if (stack.getItem() instanceof ItemAttachment) return ((ItemAttachment) stack.getItem()).getType();
        return null;
    }

    public static AttachmentType getFromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();

        if (item instanceof ItemAttachment) {
            return ((ItemAttachment) item).getType();
        }
        return null;
    }


    @Override
    public float getFOVFactor() {
        return fovZoomLevel;
    }

    @Override
    public float getViewFov() {
        return viewFov;
    }

    @Override
    public float getAimSpeed() {
        return aimSpeed;
    }

    @Override
    public BedrockAttachmentModel getAnimatedModel() {
        if (bedrockAttachmentModel == null) {
            return null;
        }
        return bedrockAttachmentModel;
    }
}
