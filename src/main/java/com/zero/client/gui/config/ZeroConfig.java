package com.zero.client.gui.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ZeroConfig {
    //更新json
    public static boolean isUpdateJson = false;
    public static boolean debug = false;

    //渲染
    public static float beam_length = 2f;
    public static float beam_width = 0.02f;

    //倍镜
    public static float sensitivityMultiplier = 1;//鼠标灵敏度
    public static float coefficient = 1.33f;//荧幕距离系数
    public static boolean aim = false;

    private static Configuration config;

    public static void init(File configDir) {
        File file = new File(configDir, "zero.cfg");
        config = new Configuration(file);
        config.load();

        isUpdateJson = config.get("client", "isUpdateJson", false).getBoolean();
        debug = config.get("client", "debug", false).getBoolean();

        //渲染
        beam_length = (float) config.get("client", "beam_length", 2).getDouble();
        beam_width = (float) config.get("client", "beam_width", 0.02).getDouble();

        //倍镜
        sensitivityMultiplier = (float) config.get("client", "sensitivityMultiplier", 1).getDouble();
        coefficient = (float) config.get("client", "coefficient", 1.33).getDouble();
        aim = config.get("client", "aim", false).getBoolean();
    }


    public static void save() {
        config.get("client", "isUpdateJson", false).set(isUpdateJson);
        config.get("client", "debug", false).set(debug);

        config.get("client", "beam_length", 2).set(beam_length);
        config.get("client", "beam_width", 0.02f).set(beam_width);

        config.get("client", "sensitivityMultiplier", 1).set(sensitivityMultiplier);
        config.get("client", "coefficient", 1.33f).set(coefficient);
        config.get("client", "aim", false).set(aim);

        config.save();
    }

}
