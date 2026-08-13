package com.zero.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zero.Zero;
import com.zero.client.animation.json.BedrockAnimationFile;
import com.zero.client.event.CameraSetupEvent;
import com.zero.client.event.ClientRenderItemEvent;
import com.zero.client.event.ClientTickEvent;
import com.zero.client.input.ZeroKeyBinding;
import com.zero.client.model.json.BedrockJson;
import com.zero.client.render.RenderBullet;
import com.zero.client.render.RenderEntityItemZero;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.client.util.ZeroResources;
import com.zero.server.ServerProxy;
import com.zero.server.entity.EntityBullet;
import com.zero.server.entity.EntityItemZero;
import com.zero.server.file.FileList;
import com.zero.server.type.InfoType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientProxy extends ServerProxy {

    @Override
    public void Pre(FMLPreInitializationEvent pre) {
        Zero.eventRegister(this);
        new ClientTickEvent();
        new CameraSetupEvent();
        new ClientRenderItemEvent();
        new ZeroKeyBinding();
    }

    @Override
    public void In(FMLInitializationEvent In) {
    }

    @Override
    public void post(FMLPostInitializationEvent post) {

    }

    @SubscribeEvent
    public void registerJsonModel(ModelRegistryEvent registry) {
        for (Item item : InfoType.getItem()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityItemZero.class, new RenderEntityItemZero.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, new RenderBullet.Factory());
    }

    @SubscribeEvent
    public void registerSound(RegistryEvent.Register<SoundEvent> event) {

    }

    @Override
    public ResourceLocation loadSound(FileList fileList, String name, String sound) {
        // 构建音效资源路径，假设音效文件放在 assets/zero/sounds/ 下，扩展名为 .ogg
        ResourceLocation location = new ResourceLocation(Zero.MOD_ID, "sounds/" + name + "/" + sound + ".ogg");
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        // 尝试获取资源，如果不存在会抛出异常
        try (IResource resource = manager.getResource(location)) {
            // 资源存在，返回 ResourceLocation
            return new ResourceLocation(Zero.MOD_ID, name + "/" + sound);
        } catch (Exception e) {
            Zero.error("加载音效失败: 包名=" + fileList.getNameContentPack() + ", 文本名称=" + fileList.getNameText() + ", 音效名称=" + sound);
            return null;
        }
    }

    @Override
    public BedrockAnimationFile loadAnimation(FileList fileList, String name) {
        // 构造资源路径
        ResourceLocation location = new ResourceLocation(Zero.MOD_ID, "animation/" + name + ".json");
        // 获取资源管理器
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        JsonObject root;
        // UTF-8字符流读取 JSON
        try (// 获取资源（Forge 会自动从目录或ZIP读取）
             IResource resource = manager.getResource(location);
             InputStream stream = resource.getInputStream();
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

            JsonParser parser = new JsonParser();
            root = parser.parse(reader).getAsJsonObject();
            // 解析 Bedrock 模型
            BedrockAnimationFile bedrockJson = new BedrockAnimationFile();
            bedrockJson.read(root);
            return bedrockJson;
        } catch (Exception e) {
            throw new RuntimeException("加载Bedrock动画失败: 包名=" + fileList.getNameContentPack() + ", 文本名称=" + fileList.getNameText(), e);
        }
    }

    @Override
    public List<String> loadTxtConfig(FileList fileList, String head, String name) {
        List<String> list = new ArrayList<>();
        // 构造资源路径
        ResourceLocation location = new ResourceLocation(Zero.MOD_ID, head + "/" + name + ".txt");
        // 获取资源管理器
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        // 获取资源（Forge 会自动从目录或ZIP读取）
        try (IResource resource = manager.getResource(location);
             InputStream stream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("加载文本失败: 包名=" + fileList.getNameContentPack() + ", 文本名称=" + fileList.getNameText(), e);
        }
        return list;
    }


    @Override
    public BedrockJson loadModel(FileList fileList, String name) {
        // 构造资源路径
        ResourceLocation location = new ResourceLocation(Zero.MOD_ID, "geo_model/" + name + ".json");
        // 获取资源管理器
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        JsonObject root;
        // UTF-8字符流读取 JSON
        try (IResource resource = manager.getResource(location);
             InputStream stream = resource.getInputStream();
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

            JsonParser parser = new JsonParser();
            root = parser.parse(reader).getAsJsonObject();
            // 解析 Bedrock 模型
            BedrockJson bedrockJson = new BedrockJson();
            bedrockJson.read(root);
            return bedrockJson;
        } catch (Exception e) {
            throw new RuntimeException("加载Bedrock模型失败: 包名=" + fileList.getNameContentPack() + ", 文本名称=" + fileList.getNameText(), e);
        }
    }

    @Override
    public void forceReload() {
        FMLClientHandler.instance().refreshResources(VanillaResourceType.MODELS, VanillaResourceType.TEXTURES, VanillaResourceType.SOUNDS, VanillaResourceType.LANGUAGES);
    }

    //激活外部资源
    @Override
    public List<File> getContentList(File dir) {
        List<File> contentPacks = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory() || zipJar(file.getName())) {
                try {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("modid", Zero.MOD_ID);
                    map.put("name", "Zero: " + file.getName());
                    map.put("version", "1");
                    FMLModContainer container = new FMLModContainer("com.zero.Zero", new ModCandidate(file, file, file.isDirectory() ? ContainerType.DIR : ContainerType.JAR), map);
                    container.bindMetadata(MetadataCollection.from(null, ""));
                    FMLClientHandler.instance().addModAsResource(container);
                } catch (Exception e) {
                    Zero.error("无法加载资源目录: " + file.getName());
                    e.printStackTrace();
                }
                Zero.info("加载内容包: " + file.getName());
                contentPacks.add(file);
            }
        }
        Zero.info("所有外部加载资源完成.");
        return contentPacks;
    }

    @SideOnly(Side.CLIENT)
    public static Particle getParticle(String particleName, World world, double x, double y, double z) {
        EnumParticleTypes particle = EnumParticleTypes.getByName(particleName);
        if (particle == null) {
            return null;
        }
        return getMinecraft().effectRenderer.spawnEffectParticle(particle.getParticleID(), x, y, z, 0D, 0D, 0D);
    }


    public static Minecraft getMinecraft() {
        return FMLClientHandler.instance().getClient();
    }
}
