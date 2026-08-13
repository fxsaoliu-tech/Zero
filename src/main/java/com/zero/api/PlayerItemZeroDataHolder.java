package com.zero.api;

import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.server.item.ItemZero;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

public class PlayerItemZeroDataHolder {
    //物品
    private int oldSlot = -1;
    private ItemStack oldStack = ItemStack.EMPTY.copy();
    //物品数据
    private ItemZero itemZero;
    //抬起
    private long clientDrawTimestamp = -1;
    private boolean drawing = false;
    //收起
    private long clientPutAwayTimestamp = -1;
    private long currentPutAwayTimeOff = -1;
    private boolean putAway = false;
    //包含动画和逻辑层
    private ItemZeroStateMachine stateMachine;

    private EntityPlayerSP player;


    public PlayerItemZeroDataHolder(EntityPlayerSP player) {
        this.player = player;
    }

    public synchronized void onUpdate() {
        if (putAway) {
            return;
        }
        int slot = player.inventory.currentItem;
        ItemStack stack = player.inventory.getStackInSlot(slot);

        boolean isGun = isItemZero(stack);
        boolean wasGun = isItemZero(oldStack);

        if (ItemStack.areItemsEqual(oldStack, stack) && slot == oldSlot) {
            return;
        }
        if (!wasGun && isGun) {
            itemZero = (ItemZero) stack.copy().getItem();
            clientDrawTimestamp = System.currentTimeMillis();
            stateMachine = itemZero.getStateMachine(this);
            stateMachine.tryInit(player);
            oldSlot = slot;
            oldStack = stack.copy();
        }
        if (wasGun) {
            ItemStack current = player.inventory.getStackInSlot(oldSlot).copy();
            clientPutAwayTimestamp = System.currentTimeMillis();
            long millis = updatePutRemainingTime();
            if (isItemZero(current)) {
                stateMachine.tryExit(current, millis);
            } else {
                stateMachine.tryExit(oldStack, millis);
            }
            putAway = true;
        }
    }

    //完成绘制
    public boolean isDraw() {
        if (!drawing) {
            if (itemZero != null) {
                drawing = (System.currentTimeMillis() - clientDrawTimestamp) / 1000f >= itemZero.getType().drawTime;
            }
        }
        return drawing;
    }

    //获取draw完成进度条
    public float getProgress() {
        long elapsed = System.currentTimeMillis() - clientDrawTimestamp;
        float progress = elapsed / 1000f / itemZero.getType().drawTime;
        return Math.min(1.0f, Math.max(0.0f, progress));
    }

    public boolean isBuy() {
        return isDraw() && !putAway;
    }

    public long updatePutRemainingTime() {
        // draw进度 (0~1)
        float drawProgress = getProgress();
        // 总毫秒
        long totalMillis = (long) (itemZero.getType().putAwayTime * 1000);
        currentPutAwayTimeOff = (long) (totalMillis * (1.0f - drawProgress));
        return totalMillis - currentPutAwayTimeOff;
    }

    //释放锁定逻辑
    public void updatePut() {
        if (putAway) {
            long now = System.currentTimeMillis();
            float delta = ((now - clientPutAwayTimestamp) + currentPutAwayTimeOff) / 1000f;
            if (delta >= (itemZero.getType().putAwayTime)) {
                putAway = false;
                reset();
                onUpdate();
            }
        }
    }

    public boolean getSprinting(boolean sprinting) {
        if (stateMachine == null || itemZero == null) {
            return sprinting;
        }
        return stateMachine.getSprinting(sprinting);
    }


    public void clear() {
        reset();
        stateMachine = null;
    }

    private void reset() {
        oldSlot = -1;
        oldStack = ItemStack.EMPTY.copy();

        itemZero = null;
        drawing = false;
        clientDrawTimestamp = -1;

        putAway = false;
        clientPutAwayTimestamp = -1;
        currentPutAwayTimeOff = -1;
    }

    public ItemZeroStateMachine getStateMachine() {
        return stateMachine;
    }

    private boolean isItemZero(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemZero;
    }
}
