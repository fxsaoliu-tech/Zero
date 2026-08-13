package com.zero.api.client.machine.animation.gun;

import net.minecraft.entity.player.EntityPlayer;

public class RecoilSystem {
    // 当前需要回弹的角度
    private float recoilPitch;
    // 回弹比例
    private float recovery = 0.5F;

    public RecoilSystem() {

    }

    /**
     * 开枪增加后坐力
     */
    public void addRecoil(EntityPlayer player, float pitch, float yaw) {
        // 直接改变玩家视角
        player.rotationPitch -= pitch;
        if (Math.random() > 0.5f) {
            // 水平随机
            float horizontal = (float) ((Math.random() - 0.5F) * yaw);
            player.rotationYaw += horizontal;
        }
        // 记录需要回弹的部分
        recoilPitch += pitch * recovery;
    }


    /**
     * 回弹
     */
    public void update(EntityPlayer player) {
        if (recoilPitch != 0) {
            float value = recoilPitch * 0.2F;
            player.rotationPitch += value;
            recoilPitch -= value;
        }
    }

}