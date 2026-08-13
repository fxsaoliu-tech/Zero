package com.zero.server.item;

import com.zero.api.PlayerItemZeroDataHolder;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.server.data.GunData;
import com.zero.server.entity.EntityBullet;
import com.zero.server.entity.EntityItemZero;
import com.zero.server.inventory.InventoryHelper;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.BulletType;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.GunFireType;
import com.zero.server.type.mode.IAttachmentType;
import com.zero.server.type.tab.EnumTabType;
import com.zero.server.type.tab.TabUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemGun extends Item implements ItemZero {
    private GunType type;

    public ItemGun(GunType type) {
        this.type = type;
        this.setTranslationKey(type.id);//注册本地化
        this.setRegistryName(type.id);//注册名
        this.setMaxStackSize(1);//设置物品叠
        type.item = this;
        TabUtil.setTabs(type);
    }


    //获取弹夹最大容量
    public int getMaxMagSize(ItemStack stack) {
        int baseAmmo = type.ammo_amount;
        int capacityLevel = type.getMagAmmoLevel(stack);
        if (capacityLevel != 0) {
            return baseAmmo + type.extended_mag_ammo_amount[(capacityLevel - 1)];
        }
        return baseAmmo;
    }

    //获取扩容弹夹等级容量
    public int getMagSize(ItemStack stack) {
        int capacityLevel = type.getMagAmmoLevel(stack);
        if (capacityLevel != 0) {
            return type.extended_mag_ammo_amount[(capacityLevel - 1)];
        }
        return 0;
    }

    //设置子弹容量
    public void setAmmo(ItemStack gun, String type, int number) {
        if (!gun.hasTagCompound()) {
            gun.setTagCompound(new NBTTagCompound());
        }
        String currentAmmo = "currentAmmo";
        String currentAmmoType = "ammoType";
        NBTTagCompound ammoTags = gun.getTagCompound();
        if (!ammoTags.hasKey(currentAmmo)) {
            ammoTags.setInteger(currentAmmo, 0);
        }
        if (!ammoTags.hasKey(currentAmmoType)) {
            ammoTags.setString(currentAmmoType, "");
        }
        ammoTags.setInteger(currentAmmo, number);
        ammoTags.setString(currentAmmoType, type);
    }

    //清空弹夹
    public void removeAmmo(ItemStack gun) {
        if (!gun.hasTagCompound()) {
            return;
        }
        String currentAmmo = "currentAmmo";
        String currentAmmoType = "ammoType";
        NBTTagCompound ammoTags = gun.getTagCompound();
        if (ammoTags.hasKey(currentAmmo)) {
            ammoTags.setInteger(currentAmmo, 0);
        }
        if (ammoTags.hasKey(currentAmmoType)) {
            ammoTags.setString(currentAmmoType, "");
        }
    }

    //获取当前子弹数量
    public int getCurrentAmmo(ItemStack gun) {
        NBTTagCompound tag = gun.getTagCompound();
        if (tag == null || !tag.hasKey("currentAmmo")) {
            return 0;
        }
        return tag.getInteger("currentAmmo");
    }

    //获取当前子弹属性
    public String getCurrentAmmoType(ItemStack gun) {
        NBTTagCompound tag = gun.getTagCompound();
        if (tag == null || !tag.hasKey("ammoType")) {
            return "";
        }
        return tag.getString("ammoType");
    }

    /**
     * @param gun       枪械
     * @param inventory 玩家主背包
     * @return 可以重载枪械弹药
     */
    public static boolean canReload(ItemStack gun, IInventory inventory) {
        ItemGun itemGun = (ItemGun) gun.getItem();
        GunType gunType = itemGun.getType();
        if (!isAmmoSufficient(gun)) {
            return false;
        }
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (gunType.isAmmo(stack)) {
                return true;
            }
        }
        return false;
    }

    //检测枪械弹药是否充足
    public static boolean isAmmoSufficient(ItemStack gun) {
        ItemGun itemGun = (ItemGun) gun.getItem();
        return !(itemGun.getCurrentAmmo(gun) == itemGun.getMaxMagSize(gun));
    }

    public boolean reload(ItemStack gunStack, World world, Entity entity, IInventory inventory, boolean isCreative) {
        int maxAmmo = this.getMaxMagSize(gunStack);
        int currentAmmo = this.getCurrentAmmo(gunStack);
        if (currentAmmo == maxAmmo) {
            return false;
        }
        //如果存在 就直接找这个的 如果没有 用isAmmo判断的
        String currentType = this.getCurrentAmmoType(gunStack);
        //创造模式无消耗
        if (isCreative) {
            if (currentType.isEmpty()) {
                if (type.Ammo != null) {
                    setAmmo(gunStack, type.Ammo.id, maxAmmo);
                }
            } else {
                setAmmo(gunStack, currentType, maxAmmo);
            }
            return true;
        }
        //需要装填的弹药
        int needAmmo = maxAmmo - currentAmmo;
        //最终目标弹夹
        String targetType = currentType;
        List<Integer> slots = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        // ===== 2. 从背包中选择子弹=====
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!type.isAmmo(stack)) {
                continue;
            }
            BulletType bulletType = BulletType.getBulletType(stack);
            if (bulletType == null) {
                continue;
            }
            if (targetType.equalsIgnoreCase("")) {
                targetType = bulletType.id;
            }
            if (!bulletType.id.equalsIgnoreCase(targetType)) {
                continue;
            }
            int count = stack.getCount();
            int take = Math.min(needAmmo, count);   // 这个格子要拿走多少发

            slots.add(i);
            numbers.add(take);
            needAmmo -= take;   // 需求减少
            if (needAmmo <= 0) {
                break;
            }
        }
        //背包没有可用弹夹
        if (slots.isEmpty()) {
            return false;
        }
        // 实际装填的弹药数 = 最初总需求 - 最后剩余未满足的需求
        int totalLoaded = (maxAmmo - currentAmmo) - needAmmo;
        // ===== 扣除阶段：严格按 numbers 从原堆叠扣除 =====
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            int take = numbers.get(i);
            ItemStack stack = inventory.getStackInSlot(slot);   // 直接拿原始引用

            if (stack.getCount() == take) {
                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);   // 直接置空
            } else {
                stack.setCount(stack.getCount() - take);   // 修改原堆叠数量
            }
        }
        inventory.markDirty();
        this.setAmmo(gunStack, targetType, currentAmmo + totalLoaded);
        return true;
    }


    public boolean unload(ItemStack gunStack, World world, Entity entity, IInventory inventory, boolean isCreative) {
        int currentAmmo = getCurrentAmmo(gunStack);
        if (currentAmmo <= 0) {
            return false;
        }
        String ammoType = getCurrentAmmoType(gunStack);
        if (ammoType.isEmpty()) {
            removeAmmo(gunStack);
            return false;
        }
        BulletType bulletType = BulletType.getBulletType(ammoType);
        if (bulletType == null || bulletType.item == null) {
            return false;
        }
        ItemStack bulletStack = new ItemStack(bulletType.item, currentAmmo);
        // 先放背包
        InventoryHelper.addItemStackToInventory(inventory, bulletStack, isCreative);
        // 放不下的掉地上
        if (!bulletStack.isEmpty() && bulletStack.getCount() > 0 && !world.isRemote) {
            EntityItem entityItem = new EntityItem(world, entity.posX, entity.posY + 0.5, entity.posZ, bulletStack.copy());
            world.spawnEntity(entityItem);
        }
        inventory.markDirty();
        // 清空弹夹
        removeAmmo(gunStack);
        return true;
    }

    public void shootClient(ItemStack gunStack) {
        int ammo = getCurrentAmmo(gunStack);
        String type = getCurrentAmmoType(gunStack);
        BulletType bulletType = BulletType.getBulletType(type);
        if (bulletType == null) {
            return;
        }
        ammo--;
        if (ammo == 0) {
            removeAmmo(gunStack);
        }
    }

    public void shootServer(EntityPlayer player, ItemStack gunStack, float aim, World world, GunData gunData) {
        int ammo = getCurrentAmmo(gunStack);
        String type = getCurrentAmmoType(gunStack);
        BulletType bulletType = BulletType.getBulletType(type);
        if (bulletType == null) {
            return;
        }
        if (!gunData.isCanShoot()) {
            return;
        }
        ammo--;
        long time = (60000 / this.type.shootRpm) - 100;

        if (this.getType().getFireType(gunStack) == GunFireType.BOLT) {
            gunData.setShootInterval((long) (time + ((getType().boltTime * 1000) - 150)));
        } else {
            gunData.setShootInterval(time);
        }
        setAmmo(gunStack, type, ammo);
        if (ammo == 0) {
            removeAmmo(gunStack);
        }
        float spread = aim > 0.75f ? this.type.aimSpread : this.type.baseSpread;

        AttachmentType attachmentType = this.getType().getAttachment(gunStack, IAttachmentType.MUZZLE);
        if (attachmentType != null) {
            spread = spread - spread * attachmentType.spreadMultiplier;
        }
        for (int i = 0; i < this.type.bulletCont; i++) {
            world.spawnEntity(new EntityBullet(world, player, this.type.damage, this.type.speed, spread, bulletType, getType()));
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) {
            type.initializationDefaultSet(stack);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.zero.gun.info.show.type"));

        tooltip.add(I18n.format("tooltip.zero.gun.info.damage") + " " + type.damage);

        EnumTabType enumTabType = type.enumTabType;
        String infoType = I18n.format("tooltip.zero.gun.info.type");
        if (enumTabType != null) {
            tooltip.add(infoType + " " + I18n.format("itemGroup.tabZero." + enumTabType.getName()));
        }

        BulletType bulletType = type.Ammo;
        String text = I18n.format("tooltip.zero.gun.info.show");
        if (bulletType != null) {
            tooltip.add(I18n.format(text + " " + I18n.format("item." + bulletType.id + ".name")));
        } else {
            tooltip.add(text + " null");
        }
        tooltip.add(I18n.format("tooltip.zero.gun.info.ammo") + " §2" + this.getCurrentAmmo(stack) + " §6/ §4" + this.getMaxMagSize(stack));


        if (GuiScreen.isShiftKeyDown()) {
            List<AttachmentType> list = type.attachmentAll;
            List<AttachmentType> scope = new ArrayList<>();
            List<AttachmentType> muzzle = new ArrayList<>();
            List<AttachmentType> stock = new ArrayList<>();
            List<AttachmentType> grip = new ArrayList<>();
            List<AttachmentType> laser = new ArrayList<>();
            List<AttachmentType> extended_mag = new ArrayList<>();
            for (AttachmentType attachmentType : list) {
                switch (attachmentType.enumAttachmentType) {
                    case MUZZLE:
                        muzzle.add(attachmentType);
                        break;
                    case GRIP:
                        grip.add(attachmentType);
                        break;
                    case STOCK:
                        stock.add(attachmentType);
                        break;
                    case LASER:
                        laser.add(attachmentType);
                        break;
                    case EXTENDED_MAG:
                        extended_mag.add(attachmentType);
                        break;
                    case SCOPE:
                        scope.add(attachmentType);
                        break;
                }
            }
            tooltip.add(I18n.format("tooltip.zero.info.attachment"));
            writeLore(type.allowScopeAttachments, tooltip, scope, "tooltip.zero.attachment.scope");
            writeLore(type.allowMuzzleAttachments, tooltip, muzzle, "tooltip.zero.attachment.muzzle");
            writeLore(type.allowStockAttachments, tooltip, stock, "tooltip.zero.attachment.stock");
            writeLore(type.allowGripeAttachments, tooltip, grip, "tooltip.zero.attachment.grip");
            writeLore(type.allowLaserAttachments, tooltip, laser, "tooltip.zero.attachment.laser");
            writeLore(type.allowMagAttachments, tooltip, extended_mag, "tooltip.zero.attachment.extended_mag");
            tooltip.add(I18n.format("tooltip.zero.info.z"));
        } else {
            tooltip.add(I18n.format("tooltip.zero.info.shift"));
        }
    }

    private static void writeLore(boolean show, List<String> lore, List<AttachmentType> list, String keyAttachment) {
        if (show && !list.isEmpty()) {
            StringBuilder sc = new StringBuilder(I18n.format(keyAttachment) + ": ");
            for (int i = 0; i < list.size(); i++) {
                sc.append(I18n.format("item." + list.get(i).id + ".name"));
                sc.append(" ");
                if (i == 8) {
                    int number = list.size() - 9;
                    if (number > 0) {
                        sc.append("§4").append(number).append("+");
                    }
                    break;
                }
            }
            lore.add(sc.toString());
        }
    }

    //更新方块
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
        return true;
    }


    //可以破坏方块
    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        return false;
    }

    //右键物品会发生的事
    @Override
    public boolean canItemEditBlocks() {
        return false;
    }

    //关于实体摇摆物品
    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return true;
    }

    //有自定义实体吗 （物品在世界生成的实体）
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    //自定义实体 （物品在世界生成的实体）
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (!world.isRemote) {
            return new EntityItemZero((EntityItem) location);
        }
        return null;
    }

    //打开nbt同步
    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack item) {
        return 72000;
    }

    @Override
    public ItemZeroStateMachine getStateMachine(PlayerItemZeroDataHolder holder) {
        return new GunStateMachine(this, holder);
    }

    @Override
    public GunType getType() {
        return type;
    }
}
