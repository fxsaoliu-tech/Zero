package com.zero.api.client;

public interface ItemFov {

    //获取世界fov视野缩放
    float getWorldFov(float originalFOV);

    //获取手部fov视野缩放
    float getHandFov(float originalFOV);

    //获取鼠标灵敏度
    double getSensitivity();

    //获取瞄准进度（0-1）
    float getAimingProgress();

}
