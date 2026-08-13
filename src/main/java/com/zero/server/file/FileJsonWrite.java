package com.zero.server.file;

import com.zero.Zero;
import com.zero.server.ServerProxy;
import com.zero.server.type.InfoType;
import net.minecraft.client.Minecraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileJsonWrite {

    public static void createAll(File dir) {
        Map<String, List<FileData>> date = FileData.JsonData;
        for (String content : date.keySet()) {
            List<FileData> dates = date.get(content);
            List<String> lang = new ArrayList<>();
            if (!ServerProxy.zipJar(content)) {
                for (FileData fileDate : dates) {
                    createJSon(new File(dir, content + "/assets/" + Zero.MOD_ID), fileDate.fileList.getType(), fileDate.infoType);
                    lang.add("item." + fileDate.infoType.id + ".name=" + fileDate.infoType.name.replace("&", "§"));
                }
                String langFormat = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
                saveToFile(new File(dir, content + "/assets/" + Zero.MOD_ID + "/lang/" + langFormat + ".lang"), lang);
            }
        }
        FileData.JsonData.clear();
    }

    private static void createJSon(File path, FileType type, InfoType infoType) {
        List<List<String>> json = getJson(type, infoType);
        switch (type) {
            case GUN:
            case BULLET:
            case ATTACHMENT:
                File file = new File(path, "models/item/" + infoType.id + ".json");
                if (!file.exists()) {
                    saveToFile(new File(path, "models/item/" + infoType.id + ".json"), json.get(0));
                }
                break;
        }
    }

    private static List<List<String>> getJson(FileType type, InfoType infoType) {
        List<List<String>> list = new ArrayList<>();
        switch (type) {
            case GUN:
            case BULLET:
            case ATTACHMENT:
                list.add(getOrdinaryJson(type, infoType));
                break;
        }
        return list;
    }

    private static void saveToFile(File file, List<String> content) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : content) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            Zero.logger.error(e);
        }
    }

    private static List<String> getOrdinaryJson(FileType type, InfoType infoType) {
        List<String> json = new ArrayList<>();
        json.add("{");
        json.add("  \"parent\": \"item/generated\",");
        json.add("  \"textures\": {");
        json.add("    \"layer0\": \"" + Zero.MOD_ID + ":" + type.getName() + "/slot/" + infoType.icon + "\"");
        String a = infoType.existModel() ? "  }," : "  }";
        json.add(a);
        if (infoType.existModel()) {
            json.add("  \"display\": {");
            json.add("    \"thirdperson_righthand\": {");
            json.add("      \"rotation\": [90, 90, -35],");
            json.add("      \"translation\": [2, -1, 1],");
            json.add("      \"scale\": [0, 0, 0]");
            json.add("    },");
            json.add("    \"thirdperson_lefthand\": {");
            json.add("      \"rotation\": [90, -90, 35],");
            json.add("      \"translation\": [2, -1, 1],");
            json.add("      \"scale\": [0, 0, 0]");
            json.add("    }");
            json.add("  }");
        }
        json.add("}");
        return json;
    }

}
