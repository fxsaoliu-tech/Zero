package com.zero.server.util;

public class ZeroTimer {
    private boolean running;
    private long startTime;
    private long duration;


    public ZeroTimer() {
        reset();
    }


    /**
     * 开始计时
     * @param duration 持续时间(ms)
     */
    public void start(long duration) {
        if (running) {
            return;
        }
        this.running = true;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }


    /**
     * 是否正在计时
     */
    public boolean isRunning() {
        return running;
    }


    /**
     * 是否完成
     */
    public boolean isFinished() {
        if (!running) {
            return false;
        }
        return System.currentTimeMillis() - startTime >= duration;
    }


    /**
     * 获取已经过去时间
     */
    public long getElapsedTime() {
        if (!running) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }


    /**
     * 获取剩余时间
     */
    public long getRemainingTime() {
        if (!running) {
            return 0;
        }
        long remaining = duration - getElapsedTime();
        return Math.max(remaining, 0);
    }


    /**
     * 获取进度 0-1
     */
    public float getProgress() {
        if (!running || duration <= 0) {
            return 0F;
        }

        return Math.min(1F, (float)getElapsedTime() / duration);
    }


    /**
     * 强制结束
     */
    public void reset() {
        running = false;
        startTime = 0;
        duration = 0;
    }
}