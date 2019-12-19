package org.ta4j.core.trading.sltp;

import org.ta4j.core.Order;


public interface StopLossManager {
    boolean set(int index, Order order);
}
