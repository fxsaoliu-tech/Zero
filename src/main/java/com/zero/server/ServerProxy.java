package com.zero.server;

import com.zero.Zero;
import com.zero.client.animation.json.BedrockAnimationFile;
import com.zero.client.model.json.BedrockJson;
import com.zero.server.file.FileList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerProxy {

    public void Pre(FMLPreInitializationEvent pre) {
    }

    public void In(FMLInitializationEvent In) {
    }

    public void post(FMLPostInitializationEvent post) {

    }

    public ResourceLocation loadSound(FileList fileList, String name, String sound) {
        return null;
    }

    //强制刷新资源
    public void forceReload() {
    }

    public List<String> loadTxtConfig(FileList fileList, String head, String name) {
        return null;
    }

    public BedrockAnimationFile loadAnimation(FileList fileList, String name){
        return null;
    }

    public BedrockJson loadModel(FileList fileList, String path){
        return null;
    }

    public List<File> getContentList(File dir) {
        List<File> contentPacks = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory() || zipJar(file.getName())) {
                Zero.info("内容包: " + file.getName() + " 加载完成");
                contentPacks.add(file);
            }
        }
        Zero.info("加载外部资源完成.");
        return contentPacks;
    }

    public static boolean zipJar(String name) {
        return name.endsWith(".zip") || name.endsWith(".jar");
    }

}
