package org.ta4j.core.trading.sltp;

import org.ta4j.core.Order;


public interface TakeProfitManager {
    boolean set(int index, Order order);
    void setTrail(boolean trail);
    boolean getTrail();
}
