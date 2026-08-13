package com.zero.network;

import com.zero.Zero;
import com.zero.network.gun.*;
import com.zero.network.hit.PacketHitMarker;
import com.zero.network.hit.PacketKillMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@ChannelHandler.Sharable
public class PacketHandler extends MessageToMessageCodec<FMLProxyPacket, PacketBase> {
    //通讯
    private EnumMap<Side, FMLEmbeddedChannel> channels;
    //确认Zero是否初始化 初始化完成不能再注册数据包
    private boolean modInitialised = false;
    //注册数据列表
    private List<Class<? extends PacketBase>> packets = new ArrayList<>();
    //客户端和服务端数据处理
    private ConcurrentLinkedQueue<PacketBase> receivedPacketsClient = new ConcurrentLinkedQueue<>();
    private Map<String, ConcurrentLinkedQueue<PacketBase>> receivedPacketsServer = new HashMap<>();

    //进行编码
    @Override
    protected void encode(ChannelHandlerContext ctx, PacketBase msg, List<Object> out) {
        try {
            ByteBuf encodedData = Unpooled.buffer();
            Class<? extends PacketBase> cl = msg.getClass();
            if (!packets.contains(cl)) {
                throw new NullPointerException("Zero包未注册: " + cl.getCanonicalName());
            }
            byte discriminator = (byte) packets.indexOf(cl);
            //写入数据包ID
            encodedData.writeByte(discriminator);
            //PacketBase编写数据
            msg.encodeInto(ctx, encodedData);
            //将自己的数据包转换为Forge数据包通过Netty系统
            FMLProxyPacket proxyPacket = new FMLProxyPacket(new PacketBuffer(encodedData.copy()), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
            //添加到输出数列
            out.add(proxyPacket);
        } catch (Exception e) {
            Zero.logger.error("Zero错误编码数据包");
            e.printStackTrace();
        }
    }

    //解码
    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
        //加载ByteBuf
        ByteBuf encodedData = msg.payload();
        //获取可以解析本包ID
        byte discriminator = encodedData.readByte();
        Class<? extends PacketBase> cl = packets.get(discriminator);
        //为空拒绝处理数据
        if (cl == null) {
            throw new NullPointerException("Zero没有注册过的数据包: " + discriminator);
        }
        //创建新的数据包来写入原本数据
        PacketBase packet = cl.newInstance();
        packet.decodeInto(ctx, encodedData.slice());
        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT: {
                receivedPacketsClient.offer(packet);
                break;
            }
            case SERVER: {
                INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                EntityPlayer player = ((NetHandlerPlayServer) netHandler).player;
                if (!receivedPacketsServer.containsKey(player.getName())) {
                    receivedPacketsServer.put(player.getName(), new ConcurrentLinkedQueue<>());
                }
                receivedPacketsServer.get(player.getName()).offer(packet);
                break;
            }
        }
    }

    public boolean registerPacket(Class<? extends PacketBase> cl) {
        if (packets.size() > 256) {
            Zero.logger.warn("Zero数据包已达最大处理程度停止注册:{}.", cl.getCanonicalName());
            return false;
        }
        if (packets.contains(cl)) {
            Zero.logger.warn("Zero已有相同数据包: {} 停止注册", cl.getCanonicalName());
            return false;
        }
        if (modInitialised) {
            Zero.logger.warn("Zero已完成初始化停止注册: {}", cl.getCanonicalName());
            return false;
        }
        packets.add(cl);
        return true;
    }

    //初始化通讯处理程序 在开始FMLInitialise初始化
    public void initialise() {
        channels = NetworkRegistry.INSTANCE.newChannel(Zero.NAME, this);
        registerPacket(PacketGunReload.class);
        registerPacket(PacketGunFireSelect.class);
        registerPacket(PacketGunFire.class);
        registerPacket(PacketGunUnloadAttachment.class);
        registerPacket(PacketGunRefitGun.class);
        registerPacket(PacketGunRefreshRefitScreen.class);


        registerPacket(PacketHitMarker.class);
        registerPacket(PacketKillMessage.class);
    }

    //从 FMLPostInitialise方法开始初始化排序
    public void postInitialise() {
        if (modInitialised) {
            return;
        }
        modInitialised = true;
        //重新排序数据包
        packets.sort((c1, c2) -> {
            int com = String.CASE_INSENSITIVE_ORDER.compare(c1.getCanonicalName(), c2.getCanonicalName());
            if (com == 0)
                com = c1.getCanonicalName().compareTo(c2.getCanonicalName());
            return com;
        });
    }

    //处理客户端数据
    public void handleClientPackets() {
        for (PacketBase packet = receivedPacketsClient.poll(); packet != null; packet = receivedPacketsClient.poll()) {
            packet.handleClientSide(getClientPlayer());
        }
    }

    //处理服务端数据
    public void handleServerPackets() {
        for (String playerName : receivedPacketsServer.keySet()) {
            ConcurrentLinkedQueue<PacketBase> receivedPacketsFromPlayer = receivedPacketsServer.get(playerName);
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
            for (PacketBase packet = receivedPacketsFromPlayer.poll(); packet != null; packet = receivedPacketsFromPlayer.poll()) {
                packet.handleServerSide(player);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    //发送给所有玩家服务端使用
    public void sendToAll(PacketBase packet) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    //发送给指定玩家
    public void sendToPlayer(PacketBase packet, EntityPlayerMP player) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    //像周围玩家发送数据包
    public void sendToAllAround(PacketBase packet, NetworkRegistry.TargetPoint point) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    //发送给所有维度的玩家
    public void sendToDimension(PacketBase packet, int dimensionID) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionID);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    //发送给服务端
    public void sendToServer(PacketBase packet) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(packet);
    }

    //原版数据处理
    //发送给所有玩家服务端使用
    public void sendToAll(Packet packet) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayers(packet);
    }

    //发送给指定玩家
    public void sendToPlayer(Packet packet, EntityPlayerMP player) {
        player.connection.sendPacket(packet);
    }

    //发送给周围玩家
    public void sendToAllAround(Packet packet, NetworkRegistry.TargetPoint point) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendToAllNearExcept(null, point.x, point.y, point.z, point.range, point.dimension, packet);
    }

    //发送给所有维度玩家
    public void sendToDimension(Packet packet, int dimensionID) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayersInDimension(packet, dimensionID);
    }

    //发送给服务端
    public void sendToServer(Packet packet) {
        Minecraft.getMinecraft().player.connection.sendPacket(packet);
    }

    //发送给周围玩家
    public void sendToAllAround(PacketBase packet, double x, double y, double z, float range, int dimension) {
        sendToAllAround(packet, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
    }
}
