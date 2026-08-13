package com.zero.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.Name("zero")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ZeroCoreMod implements IFMLLoadingPlugin {

    public ZeroCoreMod() {
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        System.out.println(">>> CoreMod INIT <<<");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.zero.json");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}