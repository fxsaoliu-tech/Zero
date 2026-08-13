package com.zero.server.type;

import com.zero.Zero;
import com.zero.client.animation.json.BedrockAnimationFile;
import com.zero.client.animation.data.GunAnimationData;
import com.zero.client.model.BedrockGunModel;
import com.zero.client.model.bedrock.BedrockModel;
import com.zero.client.model.display.MuzzleFlashText;
import com.zero.client.model.display.ShellText;
import com.zero.client.model.display.TextShowText;
import com.zero.server.file.FileList;
import com.zero.server.item.ItemAttachment;
import com.zero.server.item.ItemBullets;
import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GunType extends InfoType implements IScope {
    //枪械默认可存储子弹
    public int ammo_amount = 10;
    //扩容弹夹
    public int[] extended_mag_ammo_amount = new int[3];
    /**
     * 可使用的弹药类型映射，键为弹药名称，值为对应子弹类型
     */
    public BulletType Ammo = null;
    //枪械伤害
    public float damage = 1.0f;
    /**
     * 重载所需时间 秒数
     */
    public float reloadEmptyTime = 0;//空仓重载时间
    public float reloadTacticalTime = 0;//非空仓重载时间

    //射击系统的rpm
    public int shootRpm = 350;
    //拉栓时间
    public float boltTime = 1.0f;
    public float sprintShootTime = 0.2f;//跑射延迟
    public GunFireType[] fireType;
    //子弹实体生成的数量默认1
    public int bulletCont = 1;
    //枪械初始速度
    public float speed = 3.0f;
    //普通散布范围
    public float baseSpread = 3;
    //瞄准散布范围
    public float aimSpread = 2;
    //枪械后坐力
    public float recoilVertical = 1;//垂直后坐力
    public float recoilHorizontal = 1;//水平后坐力

    //音效系统
    public String shootSound = "";//枪械射击音效
    public String boltSound = "";//拉栓音效
    public String inspectEmptySound = ""; //枪械没有子弹检视音效
    public String reloadTacticalSound = "";//枪械有子弹换弹音效
    public String reloadEmptySound = "";//枪械没有子弹换弹音效
    public String dryFireSound = "";//弹匣打空后扣动扳机时播放
    public String fireSelectSound = "";//切换射击模式播放

    //配件系统槽是否启用
    public boolean allowScopeAttachments = false;//倍镜
    public boolean allowMuzzleAttachments = false;//枪口
    public boolean allowLaserAttachments = false;//激光
    public boolean allowMagAttachments = false;//扩容弹夹
    public boolean allowGripeAttachments = false;//握把
    public boolean allowStockAttachments = false;//枪托
    //可装备配件
    public List<AttachmentType> attachmentAll = new ArrayList<>();
    //瞄准
    public float aimSpeed;//瞄准速度
    public float fovZoomLevel;//视野变化
    public float viewFov;//缩放等级
    @SideOnly(Side.CLIENT)
    public Map<String, TextShowText> textShowText = null;
    @SideOnly(Side.CLIENT)
    public MuzzleFlashText muzzleFlashText = null;
    @SideOnly(Side.CLIENT)
    public ShellText shellText = null;
    @SideOnly(Side.CLIENT)
    public BedrockGunModel model = null;   //模型
    @SideOnly(Side.CLIENT)
    public String textureLod;
    @SideOnly(Side.CLIENT)
    public BedrockGunModel modelLod = null;//低模
    //默认枪械资源
    private static BedrockAnimationFile defaultAnimation = Zero.server.loadAnimation(new FileList("default", "defaultGunAnimation", null), "default_gun");

    //动画
    @SideOnly(Side.CLIENT)
    public GunAnimationData gunAnimationData = null;

    public static final Map<String, GunType> gunType = new HashMap<>();

    public GunType() {

    }

    @Override
    public GunType loadContent(FileList fileList) {
        super.loadContent(fileList);
        return this;
    }

    @Override
    protected void endRead(FileList file) {
        super.endRead(file);
        gunType.put(id, this);
    }

    @Override
    protected void registerInfo(InfoType infoType) {
        super.registerInfo(this);
    }

    @Override
    protected void read(String[] content, FileList file) {
        super.read(content, file);
        //弹夹容量
        ammo_amount = Read(content, "ammo_amount", ammo_amount, file);
        if (ammo_amount > 999) {
            ammo_amount = 999;
        }
        List<String> mag_ammo = Read(content, "extended_mag_ammo_amount", new ArrayList<>(), file);
        if (mag_ammo != null && !mag_ammo.isEmpty()) {
            int index = 0;
            for (String s : mag_ammo) {
                if (index >= extended_mag_ammo_amount.length) {
                    break;
                }
                if (ammo_amount >= 999) {
                    extended_mag_ammo_amount[index] = 0;
                } else {
                    int maxAdd = 999 - ammo_amount;
                    extended_mag_ammo_amount[index] = Math.min(Integer.parseInt(s), maxAdd);
                }
                index++;
            }
        }
        //可用子弹
        String ammo = Read(content, "ammo", "", file);
        if (!ammo.isEmpty()) {
            BulletType bulletType = BulletType.getBulletType(ammo);
            if (bulletType != null) {
                this.Ammo = bulletType;
            }
        }
        //枪械伤害
        damage = Read(content, "damage", damage, file);
        if (damage > 999) {
            damage = 999;
        }
        bulletCont = Read(content, "bulletCont", bulletCont, file);
        if (bulletCont > 20) {
            bulletCont = 20;
        }
        boltTime = Read(content, "boltTime", boltTime, file);
        speed = Read(content, "speed", speed, file);
        if (speed > 20) {
            speed = 20;
        }
        baseSpread = Read(content, "baseSpread", baseSpread, file);
        if (baseSpread > 10) {
            baseSpread = 10;
        }
        aimSpread = Read(content, "aimSpread", aimSpread, file);
        if (aimSpread > 10) {
            aimSpread = 10;
        }
        //射击
        shootRpm = Read(content, "shootRpm", shootRpm, file);
        if (shootRpm > 1200) {
            shootRpm = 1200;
        }

        List<String> fireType = Read(content, "fireMode", new ArrayList<>(), file);
        if (fireType != null && !fireType.isEmpty()) {
            List<GunFireType> temp = new ArrayList<>();
            for (String type : fireType) {
                GunFireType mode = GunFireType.getFireType(type);
                if (!temp.contains(mode)) {
                    temp.add(mode);
                }
            }
            this.fireType = temp.toArray(new GunFireType[0]);
        }

        //重载弹夹时间
        reloadEmptyTime = Read(content, "reloadEmptyTime", reloadEmptyTime, file);
        reloadTacticalTime = Read(content, "reloadTacticalTime", reloadTacticalTime, file);

        allowScopeAttachments = Read(content, "allowScopeAttachments", allowScopeAttachments, file);
        allowMuzzleAttachments = Read(content, "allowMuzzleAttachments", allowScopeAttachments, file);
        allowLaserAttachments = Read(content, "allowLaserAttachments", allowScopeAttachments, file);
        allowGripeAttachments = Read(content, "allowGripeAttachments", allowScopeAttachments, file);
        allowStockAttachments = Read(content, "allowStockAttachments", allowScopeAttachments, file);
        allowMagAttachments = Read(content, "allowMagAttachments", allowMagAttachments, file);
        //配件系统
        List<String> attachmentAll = Read(content, "attachmentAll", new ArrayList<>(), file);
        if (attachmentAll != null && !attachmentAll.isEmpty()) {
            for (String s : attachmentAll) {
                if (AttachmentType.attachments.containsKey(s))
                    this.attachmentAll.add(AttachmentType.attachments.get(s));
            }
        }
    }

    @Override
    protected void readClient(String[] content, FileList file) {
        super.readClient(content, file);
        List<String> textShow = Read(content, "textShowText", new ArrayList<>(), file);
        String muzzle = Read(content, "muzzle", "", file);
        String model = Read(content, "model", "", file);
        textureLod = Read(content, "textureLod", textureLod, file);
        String modelL = Read(content, "modelLod", "", file);
        String shellTxt = Read(content, "shellTxt", "", file);
        String animation = Read(content, "animation", "", file);

        sprintShootTime = Read(content, "sprintShootTime", sprintShootTime, file);
        if (sprintShootTime > 0.5) {
            sprintShootTime = 0.5f;
        }
        recoilVertical = Read(content, "recoilVertical", recoilVertical, file);
        if (recoilVertical > 10) {
            recoilVertical = 10;
        }
        recoilHorizontal = Read(content, "recoilHorizontal", recoilHorizontal, file);
        if (recoilHorizontal > 10) {
            recoilHorizontal = 10;
        }
        aimSpeed = Read(content, "aimSpeed", 0.25f, file);
        if (aimSpeed > 1) {
            aimSpeed = 1;
        }
        fovZoomLevel = Read(content, "fovZoomLevel", 1.5f, file);
        viewFov = Read(content, "viewFov", viewFov, file);

        shootSound = readSound(content, "shootSound", shootSound, file);

        dryFireSound = readSound(content, "dryFireSound", dryFireSound, file);
        fireSelectSound = readSound(content, "fireSelectSound", fireSelectSound, file);

        inspectEmptySound = readSound(content, "inspectEmptySound", inspectEmptySound, file);

        reloadTacticalSound = readSound(content, "reloadTacticalSound", reloadTacticalSound, file);
        reloadEmptySound = readSound(content, "reloadEmptySound", reloadEmptySound, file);

        boltSound = readSound(content, "boltSound", boltSound, file);

        if (!textShow.isEmpty()) {
            textShowText = new HashMap<>(textShow.size());
            for (String bones : textShow) {
                String[] split = bones.split("\\|");
                textShowText.put(split[0], new TextShowText(Zero.server.loadTxtConfig(file, "text", split[1]), file));
            }
        }
        if (!muzzle.isEmpty()) {
            this.muzzleFlashText = new MuzzleFlashText(Zero.server.loadTxtConfig(file, "muzzle", muzzle), file);
        }
        if (!shellTxt.isEmpty()) {
            this.shellText = new ShellText(Zero.server.loadTxtConfig(file, "shell", shellTxt), file);
        }
        if (!model.isEmpty()) {
            this.model = new BedrockGunModel(Zero.server.loadModel(file, "gun/" + model));
        }
        if (!modelL.isEmpty()) {
            this.modelLod = new BedrockGunModel(Zero.server.loadModel(file, "gun/lod/" + modelL));
        }
        if (!animation.isEmpty()) {
            BedrockAnimationFile bedrockAnimationFile = Zero.server.loadAnimation(file, animation);
            gunAnimationData = new GunAnimationData(bedrockAnimationFile.animationMap, defaultAnimation.animationMap);
            for (String sounds : gunAnimationData.getAnimationSounds()) {
                if (sounds.isEmpty()) {
                    continue;
                }
                String[] split = sounds.split(":");
                if (split.length != 2) {
                    continue;
                }
                if (!split[0].equalsIgnoreCase("zero")) {
                    continue;
                }
                String[] split1 = sounds.split("/", 2);
                if (split1.length != 2) {
                    continue;
                }
                readSound(content, "", split1[1], file);
            }
        }
    }

    public boolean isAmmo(String name) {
        if (this.Ammo != null) {
            return this.Ammo.id.equalsIgnoreCase(name);
        }
        return false;
    }

    public boolean isAmmo(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemBullets) {
                return this.isAmmo(((ItemBullets) stack.getItem()).getType().id);
            }
        }
        return false;
    }

    public static GunType getGunType(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemGun) {
                return ((ItemGun) stack.getItem()).getType();
            }
        }
        return null;
    }

    public boolean allowAttachmentType(IAttachmentType type) {
        switch (type) {
            case GRIP:
                return allowGripeAttachments;
            case LASER:
                return allowLaserAttachments;
            case SCOPE:
                return allowScopeAttachments;
            case STOCK:
                return allowStockAttachments;
            case MUZZLE:
                return allowMuzzleAttachments;
            case EXTENDED_MAG:
                return allowMagAttachments;
        }
        return false;
    }

    public boolean allowAttachment(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemAttachment) {
                for (AttachmentType attachmentType : attachmentAll) {
                    if (attachmentType.id.equalsIgnoreCase(((ItemAttachment) stack.getItem()).getType().id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getMagAmmoLevel(ItemStack stack) {
        if (!stack.isEmpty()) {
            AttachmentType type = getAttachment(stack, IAttachmentType.EXTENDED_MAG);
            if (type != null) {
                return type.level;
            }
        }
        return 0;
    }

    public GunFireType getFireType(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("fireMode")) {
                return GunFireType.getFireType(stack.getTagCompound().getString("fireMode"));
            }
        }
        return GunFireType.SEMI;
    }

    //切换开火模式
    public boolean switchFireType(ItemStack stack) {
        if (fireType == null || fireType.length <= 1) {
            return false;
        }
        GunFireType current = getFireType(stack);
        int index = 0;
        for (int i = 0; i < fireType.length; i++) {
            if (fireType[i] == current) {
                index = i;
                break;
            }
        }
        index++;
        if (index >= fireType.length) {
            index = 0;
        }
        stack.getTagCompound().setString("fireMode", fireType[index].getId());
        return true;
    }


    public void initializationDefaultSet(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (!tagCompound.hasKey("fireMode")) {
            GunFireType gunFireType;
            if (fireType != null && fireType.length > 0) {
                gunFireType = fireType[0];
            } else {
                gunFireType = GunFireType.SEMI;
            }
            tagCompound.setString("fireMode", gunFireType.getId());
        }
    }

    //获取垂直后坐力
    public float getRecoilVertical(ItemStack gunStack) {
        float recoilVertical = this.recoilVertical;
        recoilVertical = getRecoilVerticalMultiplierEnd(gunStack, recoilVertical, IAttachmentType.MUZZLE);
        recoilVertical = getRecoilVerticalMultiplierEnd(gunStack, recoilVertical, IAttachmentType.GRIP);
        recoilVertical = getRecoilVerticalMultiplierEnd(gunStack, recoilVertical, IAttachmentType.STOCK);
        return Math.max(0, recoilVertical);
    }

    //获取水平后坐力
    public float getRecoilHorizontal(ItemStack gunStack) {
        float recoilHorizontal = this.recoilHorizontal;
        recoilHorizontal = getRecoilHorizontalMultiplierEnd(gunStack, recoilHorizontal, IAttachmentType.MUZZLE);
        recoilHorizontal = getRecoilHorizontalMultiplierEnd(gunStack, recoilHorizontal, IAttachmentType.GRIP);
        recoilHorizontal = getRecoilHorizontalMultiplierEnd(gunStack, recoilHorizontal, IAttachmentType.STOCK);
        return Math.max(0, recoilHorizontal);
    }

    private float getRecoilVerticalMultiplierEnd(ItemStack gunStack, float originVertical, IAttachmentType attachmentType) {
        AttachmentType muzzle = getAttachment(gunStack, attachmentType);
        if (muzzle != null) {
            originVertical += originVertical * muzzle.recoilVerticalMultiplier;
        }
        return originVertical;
    }

    private float getRecoilHorizontalMultiplierEnd(ItemStack gunStack, float originVertical, IAttachmentType attachmentType) {
        AttachmentType muzzle = getAttachment(gunStack, attachmentType);
        if (muzzle != null) {
            originVertical += originVertical * muzzle.recoilHorizontalMultiplier;
        }
        return originVertical;
    }

    //获取倍镜
    public IScope getCurrentScope(ItemStack gunStack) {
        IScope attachedScope = getAttachment(gunStack, IAttachmentType.SCOPE);
        return attachedScope == null ? this : attachedScope;
    }

    //获取配件属性
    public AttachmentType getAttachment(ItemStack gun, IAttachmentType type) {
        checkForTags(gun);
        return AttachmentType.getFromNBT(gun.getTagCompound().getCompoundTag("attachments").getCompoundTag(type.name()));
    }

    //获取配件
    public ItemStack getAttachmentItemStack(ItemStack gun, IAttachmentType type) {
        checkForTags(gun);
        return new ItemStack(gun.getTagCompound().getCompoundTag("attachments").getCompoundTag(type.name()));
    }

    //删除配件
    public void removeAttachment(ItemStack gun, IAttachmentType type) {
        checkForTags(gun);
        NBTTagCompound attachments = gun.getTagCompound().getCompoundTag("attachments");
        // ✔ 只做清空
        attachments.setTag(type.name(), new NBTTagCompound());
    }

    public void addAttachment(ItemStack gun, IAttachmentType type, ItemStack attachment) {
        checkForTags(gun);
        if (attachment.isEmpty()) return;
        NBTTagCompound attachments = gun.getTagCompound().getCompoundTag("attachments");
        // ✔ 创建新的NBT存储
        NBTTagCompound tag = new NBTTagCompound();
        // ✔ 把ItemStack写入NBT
        attachment.writeToNBT(tag);
        // ✔ 存入对应槽位
        attachments.setTag(type.name(), tag);
    }


    //检测空指标 没有就赋值空配件
    private void checkForTags(ItemStack gun) {
        //设置标签
        if (!gun.hasTagCompound()) {
            gun.setTagCompound(new NBTTagCompound());
        }
        //设置 配件标签
        if (!gun.getTagCompound().hasKey("attachments")) {
            NBTTagCompound attachmentTags = new NBTTagCompound();
            attachmentTags.setTag(IAttachmentType.EXTENDED_MAG.name(), new NBTTagCompound());
            attachmentTags.setTag(IAttachmentType.SCOPE.name(), new NBTTagCompound());
            attachmentTags.setTag(IAttachmentType.LASER.name(), new NBTTagCompound());
            attachmentTags.setTag(IAttachmentType.MUZZLE.name(), new NBTTagCompound());
            attachmentTags.setTag(IAttachmentType.GRIP.name(), new NBTTagCompound());
            attachmentTags.setTag(IAttachmentType.STOCK.name(), new NBTTagCompound());
            gun.getTagCompound().setTag("attachments", attachmentTags);
        }
    }

    @Override
    public BedrockGunModel getAnimatedModel() {
        if (model != null) {
            return model;
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
    public boolean existModel() {
        return model != null;
    }

    @Override
    public GunAnimationData getAnimationData() {
        if (gunAnimationData != null) {
            return gunAnimationData;
        }
        return null;
    }
}
