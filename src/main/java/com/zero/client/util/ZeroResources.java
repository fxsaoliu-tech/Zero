package com.zero.client.util;

import com.zero.Zero;
import com.zero.server.file.FileList;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class ZeroResources {
    //音效资源
    private static final Map<String, ResourceLocation> Sound = new HashMap<>();
    private static final Set<String> missingCacheSound = new HashSet<>();
    //模型纹理
    private static final Map<EnumTexturesType, Map<String, ResourceLocation>> TEXTURES = new HashMap<>();
    private static final Map<EnumTexturesType, Map<String, ResourceLocation>> TEXTURE_LOD_S = new HashMap<>();
    private static final ResourceLocation DEFAULT = new ResourceLocation(Zero.MOD_ID, "textures/default.png");

    static {
        for (EnumTexturesType type : EnumTexturesType.values()) {
            TEXTURES.put(type, new HashMap<>());
            TEXTURE_LOD_S.put(type, new HashMap<>());
        }
    }

    public static ResourceLocation getTextures(EnumTexturesType type, String texture) {
        if (texture == null || texture.isEmpty()) {
            return DEFAULT;
        }
        if (TEXTURES.containsKey(type)) {
            Map<String, ResourceLocation> map = TEXTURES.get(type);
            if (map.containsKey(texture)) {
                return map.get(texture);
            } else {
                ResourceLocation resource = new ResourceLocation(Zero.MOD_ID, "textures/" + type.getName() + "/models/" + texture + ".png");
                map.put(texture, resource);
                return resource;
            }
        }
        return DEFAULT;
    }

    public static ResourceLocation getLodTextures(EnumTexturesType type, String texture) {
        if (texture == null || texture.isEmpty()) {
            return DEFAULT;
        }
        if (TEXTURE_LOD_S.containsKey(type)) {
            Map<String, ResourceLocation> map = TEXTURE_LOD_S.get(type);
            if (map.containsKey(texture)) {
                return map.get(texture);
            } else {
                ResourceLocation resource = new ResourceLocation(Zero.MOD_ID, "textures/" + type.getName() + "/lod/" + texture + ".png");
                map.put(texture, resource);
                return resource;
            }
        }
        return DEFAULT;
    }


    public static ResourceLocation getSoundResource(String sound) {
        if (Sound.containsKey(sound)) {
            return Sound.get(sound);
        }
        return null;
    }

    public static void registerSound(FileList fileList, String id, String sound) {
        String name = id + "/" + sound;
        if (missingCacheSound.contains(name) || Sound.containsKey(name)) {
            return;
        }
        ResourceLocation resource = Zero.server.loadSound(fileList, id, sound);
        if (resource != null) {
            Sound.put(name, resource);
        } else {
            missingCacheSound.add(name);
        }
    }
}
