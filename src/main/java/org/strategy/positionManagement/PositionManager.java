package org.strategy.positionManagement;

public interface PositionManager {

    public abstract void onTickEvent();

    public abstract void onBarChangeEvent(int timeFrame);

    public abstract void onOneMinuteDataEvent();

    public abstract void setTakeProfit();

    public abstract void setStopLoss();
}
