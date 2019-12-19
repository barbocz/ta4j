package org.ta4j.core;

import java.util.HashMap;
import java.util.List;

public interface Core {
    int getIndex (int index, int period);
    void debugRule (int index,Rule rule,boolean satisfied);
    boolean hasOpenOrders();
    HashMap<Integer, Order> getOpenOrders();

}
