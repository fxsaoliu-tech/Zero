package com.zero;

import com.zero.client.gui.GuiHandler;
import com.zero.client.gui.config.ZeroConfig;
import com.zero.network.PacketHandler;
import com.zero.server.ServerProxy;
import com.zero.server.data.PlayerHandler;
import com.zero.server.entity.EntityBullet;
import com.zero.server.entity.EntityItemZero;
import com.zero.server.event.ServerTickEvent;
import com.zero.server.file.FileRead;
import com.zero.server.type.InfoType;
import com.zero.server.type.tab.TabUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Zero.MOD_ID, name = Zero.NAME, version = Zero.VERSION, useMetadata = true, guiFactory = "com.zero.client.gui.config.ZeroGuiFactory")
public class Zero {
    public static final String MOD_ID = "zero";
    public static final String NAME = "Zero";
    public static final String Author = "FxSao";
    public static final String VERSION = "1.0.0";
    @SidedProxy(serverSide = "com.zero.server.ServerProxy", clientSide = "com.zero.client.ClientProxy")
    public static ServerProxy server;
    @Mod.Instance(MOD_ID)
    public static Zero zero;
    public static Logger logger;//日志
    public static File modFile;//读取数据
    public static int tick;

    private final PacketHandler packetHandler = new PacketHandler();
    private final PlayerHandler playerHandler = new PlayerHandler();

    @Mod.EventHandler
    public void Pre(FMLPreInitializationEvent pre) {
        long startTime = System.currentTimeMillis();
        logger = pre.getModLog();//获取日志
        server.Pre(pre);
        info("-----------启动中-----------");
        info("作者: " + Author);
        info("版本: " + VERSION);

        modFile = new File(pre.getModConfigurationDirectory().getParentFile(), "/Zero/");
        if (!modFile.exists()) {
            modFile.mkdir();
        }
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ZeroConfig.init(pre.getModConfigurationDirectory());
        }
        TabUtil.registerCreativeTabs();
        FileRead.readContentPacks(modFile);
        TabUtil.registerCreativeTabsItem();
        server.forceReload();
        info("耗时: " + ((System.currentTimeMillis() - startTime) / 1000) + "秒");
        info("-----------启动完毕-----------");
        eventRegister(this);
        new GuiHandler();
        eventRegister(new ServerTickEvent());
    }


    @Mod.EventHandler
    public void In(FMLInitializationEvent In) {
        server.In(In);
        packetHandler.initialise();
    }

    @Mod.EventHandler
    public void post(FMLPostInitializationEvent post) {
        server.post(post);
        packetHandler.postInitialise();
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        for (Item item : InfoType.getItem()) {
            event.getRegistry().register(item);
        }
    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {

    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        registerEntity(event, "ItemZero", EntityItemZero.class, 100, 64, 20, true);
        registerEntity(event, "Bullet", EntityBullet.class, 96, 100, 50, false);
    }

    private void registerEntity(RegistryEvent.Register<EntityEntry> event, String name, Class<? extends Entity> c, int id, int trackingRange, int update, boolean syncSpeed) {
        event.getRegistry().register(new EntityEntry(c, name).setRegistryName(name));
        EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID + ":" + name), c, name, id, this, trackingRange, update, syncSpeed);
    }

    public static void eventRegister(Object o) {
        MinecraftForge.EVENT_BUS.register(o);
    }

    public static void info(String msg) {
        logger.info("信息: {}", msg);
    }

    public static void warn(String msg) {
        logger.warn("警告: {}", msg);
    }

    public static void error(String msg) {
        logger.error("错误: {}", msg);
    }

    public static PacketHandler getPacketHandler() {
        return zero.packetHandler;
    }

    public static PlayerHandler getPlayerHandler() {
        return zero.playerHandler;
    }
}
