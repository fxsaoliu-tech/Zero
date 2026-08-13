package com.zero.server.type;

import com.zero.Zero;
import com.zero.client.animation.data.AnimationData;
import com.zero.client.gui.config.ZeroConfig;
import com.zero.client.model.BedrockAnimatedModel;
import com.zero.client.util.ZeroResources;
import com.zero.server.file.FileData;
import com.zero.server.file.FileList;
import com.zero.server.type.tab.EnumTabType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoType {
    //基础信息
    public String id;//注册名(只能小写)
    public String name;//显示名
    public int maxStackSize;//默认数量为1
    public String icon;//图标
    public String texture;//模型贴图
    public List<String> description;//显示内容
    //工作台合成
    public Item item;//本身
    public boolean shapedRecipe;//是否固定合成格子
    public List<String> recipesList;//注册合成物品名
    //掉落物品几率
    public int dungeonChance; //地牢箱子生成物品几率
    public int monsterChance;//普通怪物掉落物品几率
    public int bossChance;//boss掉落物品几率
    //抬起和收起动画时间(单位秒)
    public float drawTime = 1f;
    public String drawSound = "";//抬起播放的音效
    public float putAwayTime = 1f;
    public String putAwaySound = "";//收起播放的音效
    public String inspectSound = "";//检视播放的音效
    public String killSound = "";//击杀目标播放音效
    //物品栏标签
    public EnumTabType enumTabType = null;

    //第三人称渲染 大小
    @SideOnly(Side.CLIENT)
    public float[] third;
    //展示框渲染 大小
    @SideOnly(Side.CLIENT)
    public float[] frame;
    //实体渲染 大小
    @SideOnly(Side.CLIENT)
    public float[] entity;
    //渲染颜色
    //颜色
    public float[] color;


    private static final Map<String, InfoType> INFO_TYPE = new HashMap<>();

    public InfoType() {

    }

    //加载内容
    public InfoType loadContent(FileList fileList) {
        beginRead(fileList);
        for (String line : fileList.getLines()) {
            if (line.startsWith("//")) {
                continue;
            }
            String[] split = line.split(" ");
            if (split.length < 2) {
                continue;
            }
            read(split, fileList);
        }
        endRead(fileList);
        return this;
    }


    protected void beginRead(FileList file) {

    }


    protected void endRead(FileList file) {
        if (INFO_TYPE.containsKey(id)) {
            InfoType.warn(file, "注册名已冲突: " + id);
        }
        registerInfo(this);
        if (ZeroConfig.isUpdateJson && FMLCommonHandler.instance().getSide().isClient()) {
            new FileData(file, this);
        }
    }

    protected void registerInfo(InfoType infoType) {
        INFO_TYPE.put(id, infoType);
    }

    public static InfoType getInfoType(String id) {
        if (INFO_TYPE.containsKey(id)) {
            return INFO_TYPE.get(id);
        }
        return null;
    }

    /*
     * 读取
     */
    protected void read(String[] content, FileList file) {
        name = Read(content, "name", name, file);
        id = Read(content, "id", id, file);
        maxStackSize = Read(content, "maxStackSize", maxStackSize, file);

        icon = Read(content, "icon", icon, file);
        description = Read(content, "description", description, file);
        texture = Read(content, "texture", texture, file);

        shapedRecipe = Read(content, "shapedRecipe", shapedRecipe, file);
        recipesList = Read(content, "recipes", recipesList, file);

        dungeonChance = Read(content, "dungeonChance", dungeonChance, file);
        monsterChance = Read(content, "monsterChance", monsterChance, file);
        bossChance = Read(content, "bossChance", bossChance, file);

        String type = Read(content, "typeTab", "", file);
        if (!type.isEmpty()) {
            enumTabType = EnumTabType.getEnumInfoType(type);
        }
        if (FMLCommonHandler.instance().getSide().isClient()) {
            readClient(content, file);
        }
    }

    protected void readClient(String[] content, FileList file) {
        drawTime = Read(content, "drawTime", drawTime, file);
        putAwayTime = Read(content, "putAwayTime", putAwayTime, file);

        drawSound = readSound(content, "drawSound", drawSound, file);
        putAwaySound = readSound(content, "putAwaySound", putAwaySound, file);
        inspectSound = readSound(content, "inspectSound", inspectSound, file);
        killSound = readSound(content, "killSound", killSound, file);

        List<String> thirds = Read(content, "thirdSize", new ArrayList<>(), file);
        if (!thirds.isEmpty()) {
            third = new float[]{0.5f, 0.5f, 0.5f};
            for (int i = 0; i < thirds.size(); i++) {
                third[i] = Float.parseFloat(thirds.get(i));
            }
        }
        List<String> frames = Read(content, "frameSize", new ArrayList<>(), file);
        if (!frames.isEmpty()) {
            frame = new float[]{0.25f, 0.25f, 0.25f};
            for (int i = 0; i < frames.size(); i++) {
                frame[i] = Float.parseFloat(frames.get(i));
            }
        }
        List<String> list = Read(content, "entitySize", new ArrayList<>(), file);
        if (!list.isEmpty()) {
            entity = new float[]{0.25f, 0.25f, 0.25f};
            for (int i = 0; i < list.size(); i++) {
                entity[i] = Float.parseFloat(list.get(i));
            }
        }

        List<String> colorList = Read(content, "color", new ArrayList<>(), file);
        if (!colorList.isEmpty()) {
            color = new float[]{1, 1, 1};
            if (colorList.size() > 2) {
                for (int i = 0; i < 3; i++) {
                    color[i] = Float.parseFloat(colorList.get(i)) / 255.0f;
                }
            }
        }
    }

    protected String readSound(String[] content, String key, String sound, FileList file) {
        String soundName = Read(content, key, sound, file);
        if (!soundName.isEmpty()) {
            ZeroResources.registerSound(file, id, soundName);
        }
        return soundName;
    }


    @SideOnly(Side.CLIENT)
    public boolean existModel() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public AnimationData getAnimationData() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public BedrockAnimatedModel getAnimatedModel() {
        return null;
    }

    //键值比对
    protected boolean KeyMatches(String[] split, String key) {
        return split != null && split.length > 1 && split[0].equalsIgnoreCase(key);
    }

    //读取整数
    protected int Read(String[] split, String key, int currentValue, FileList file) {
        if (KeyMatches(split, key)) {
            try {
                currentValue = Integer.parseInt(split[1]);
            } catch (Exception e) {
                InfoType.error(file, key + "格式不正确传入值非整数");
            }
        }
        return currentValue;
    }

    //读取小数点
    protected float Read(String[] split, String key, float currentValue, FileList file) {
        if (KeyMatches(split, key)) {
            try {
                currentValue = Float.parseFloat(split[1]);
            } catch (Exception e) {
                InfoType.error(file, key + ": 格式不正确传入值非小数点或整数");
            }
        }
        return currentValue;
    }

    //读取小数点
    protected double Read(String[] split, String key, double currentValue, FileList file) {
        if (KeyMatches(split, key)) {
            try {
                currentValue = Double.parseDouble(split[1]);
            } catch (Exception e) {
                InfoType.error(file, key + ": 格式不正确传入值非小数点或整数");
            }
        }
        return currentValue;
    }

    //读取布尔值
    protected boolean Read(String[] split, String key, boolean currentValue, FileList file) {
        if (KeyMatches(split, key)) {
            try {
                currentValue = Boolean.parseBoolean(split[1]);
            } catch (Exception e) {
                InfoType.error(file, key + ": 格式不正确传入值非逻辑值");
            }
        }
        return currentValue;
    }

    //读取文字
    protected String Read(String[] split, String key, String currentValue, FileList file) {
        if (KeyMatches(split, key)) {
            currentValue = split[1].replace("&", "§");
        }
        return currentValue;
    }

    //读取多行文本
    protected List<String> Read(String[] split, String key, List<String> currentValue, FileList file) {
        if (KeyMatches(split, key)) {
            if (currentValue == null) {
                currentValue = new ArrayList<>();
            }
            for (String s : split) {
                if (!s.equalsIgnoreCase(split[0])) {
                    currentValue.add(s.replace("&", "§"));
                }
            }
        }
        return currentValue;
    }

    //注册合成表时调用
    public IRecipe getRecipes(FileList fileList) {
        ItemStack outItem = new ItemStack(item, 1);
        NonNullList<Ingredient> list = NonNullList.create();
        if (recipesList != null && recipesList.size() == 9) {
            for (int j = 0; j < 9; j++) {
                String[] nameItem = recipesList.get(j).split(":");
                Item item = Item.getByNameOrId(nameItem[0]);
                if (!nameItem[0].equalsIgnoreCase("null") || item == null) {
                    list.add(j, Ingredient.fromStacks(ItemStack.EMPTY.copy()));
                    if (item == null) {
                        InfoType.warn(fileList, id + " :合成表: 第" + (j + 1) + "格子的物品名字存在错误无法识别自动替换空白");
                    }
                } else {
                    int amount = Integer.parseInt(nameItem[1]);
                    int meta = Integer.parseInt(nameItem[2]);
                    ItemStack stack = new ItemStack(item, Math.max(amount, 1), Math.max(meta, 0));
                    list.add(j, Ingredient.fromStacks(stack.copy()));
                }
            }
        } else {
            recipesList = null;
            InfoType.error(fileList, id + " :合成表: " + "格式不正确合成必须9种物品必须全部填写不想该格有物品就输null");
            return null;
        }
        recipesList = null;
        if (shapedRecipe) {
            return new ShapedRecipes(Zero.NAME, 3, 3, list, outItem.copy()).setRegistryName(item.getTranslationKey() + "_shaped");
        } else {
            return new ShapelessRecipes(Zero.NAME, outItem.copy(), list);
        }
    }

    public static List<Item> getItem() {
        List<Item> item = new ArrayList<>();
        for (String name : InfoType.INFO_TYPE.keySet()) {
            item.add(InfoType.INFO_TYPE.get(name).item);
        }
        return item;
    }

    public static List<InfoType> getInfoType() {
        List<InfoType> item = new ArrayList<>();
        for (String name : InfoType.INFO_TYPE.keySet()) {
            item.add(InfoType.INFO_TYPE.get(name));
        }
        return item;
    }

    public static PotionEffect getPotionEffect(String[] split) {
        int potionID = Integer.parseInt(split[1]);
        int duration = Integer.parseInt(split[2]);
        int amplifier = Integer.parseInt(split[3]);
        return new PotionEffect(Potion.getPotionById(potionID), duration, amplifier, false, false);
    }


    public static void warn(FileList fileList, String key) {
        Zero.warn("问题在: " + "内容包: " + " [" + fileList.getNameContentPack() + "]" + "\\" + fileList.getType().name() + "\\" + fileList.getNameText() + "其文本内容: " + key + "]");
    }

    public static void error(FileList fileList, String key) {
        Zero.error("问题在: " + "内容包: " + " [" + fileList.getNameContentPack() + "\\" + fileList.getType().name() + "\\" + fileList.getNameText() + "其文本内容: " + key + "]");
    }
}
