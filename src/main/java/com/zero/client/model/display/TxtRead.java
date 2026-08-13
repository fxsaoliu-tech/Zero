package com.zero.client.model.display;

import com.zero.client.util.render.RenderHelper;
import com.zero.server.file.FileList;
import com.zero.server.type.InfoType;

import java.util.ArrayList;
import java.util.List;

public class TxtRead {

    public TxtRead(List<String> text,FileList fileList) {
        for (String line : text) {
            if (line.startsWith("//")) {
                continue;
            }
            String[] split = line.split(" ");
            if (split.length < 2) {
                continue;
            }
            read(split, fileList);
        }
    }

    public void read(String[] split,FileList fileList) {

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
    protected String Read(String[] split, String key, String currentValue) {
        if (KeyMatches(split, key)) {
            currentValue = split[1].replaceAll("&", "§");
        }
        return currentValue;
    }

    //读取多行文本
    protected List<String> Read(String[] split, String key, List<String> currentValue) {
        if (KeyMatches(split, key)) {
            if (currentValue == null) {
                currentValue = new ArrayList<>();
            }
            for (String s : split) {
                if (!s.equalsIgnoreCase(split[0])) {
                    currentValue.add(s.replaceAll("&", "§"));
                }
            }
        }
        return currentValue;
    }

    protected int ReadColor(String[] split, String key, int defaultColor) {
        if (!KeyMatches(split, key)) return defaultColor;
        try {
            int r = Integer.parseInt(split[1]);
            int g = Integer.parseInt(split[2]);
            int b = Integer.parseInt(split[3]);
            int a = (split.length > 4) ? Integer.parseInt(split[4]) : 255;

            return RenderHelper.toARGB(a, r, g, b);

        } catch (Exception e) {
            return defaultColor;
        }
    }

}
