package com.zero.api.client.machine;

public enum EnumState {
    //基础状态
    BASE,
    IDLE,        // 持枪待机
    PUT_AWAY,     // 收起（1 → 空）
    DRAW,        // 拔出（空 → 2）
    FINAL,//主轨道动画结束
    BREATHE,//呼吸
    WALK, //走路
    SPRINT, // 疾跑
    INSPECT,//检视
    INSPECT_RETREAT,//检视完成


    //枪械专属
    AIM,         // 瞄准
    RELOAD,      // 换弹
    FIRE_MODE,//开火模式
    BOLT,//拉栓
    SHOOT,//射击
    SEMI,//单发
    BURST,//三连发
    AUTO//全自动

}
