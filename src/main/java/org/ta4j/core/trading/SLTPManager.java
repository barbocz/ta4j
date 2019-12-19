package org.ta4j.core.trading;

import org.ta4j.core.Order;
import org.ta4j.core.extendedCore.BaseTradingRecordExtended;
import org.ta4j.core.num.Num;


public interface SLTPManager {

    default String getParameters() {return "";}

    default String getName() {return "";}

    void process(int index,Order.OrderType orderType);

    void setCore(BaseTradingRecordExtended core) ;


}
