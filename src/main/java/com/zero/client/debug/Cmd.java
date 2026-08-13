package com.zero.client.debug;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class Cmd extends CommandBase {
    public static float x;
    public static float y;
    public static float z;
    public static float width;
    public static float height;

    @Override
    public String getName() {
        return "hud";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "hud";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        x = Float.parseFloat(args[0]);
        y = Float.parseFloat(args[1]);
        z = Float.parseFloat(args[2]);
        width = Float.parseFloat(args[3]);
        height = Float.parseFloat(args[4]);
    }
}
