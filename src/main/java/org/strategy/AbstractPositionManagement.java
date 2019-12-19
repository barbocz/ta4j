package org.strategy;

import org.ta4j.core.extendedCore.BaseTradingRecordExtended;
import org.ta4j.core.trading.SLTPManager;

import java.util.ArrayList;
import java.util.List;

public class AbstractPositionManagement {
    public List<SLTPManager>  exitLevelsForBuy=new ArrayList<>();
    public List<SLTPManager> exitLevelsForSell=new ArrayList<>();

    public void setCore(BaseTradingRecordExtended core) {
        for (SLTPManager tpBuy: exitLevelsForBuy) tpBuy.setCore(core);
        for (SLTPManager tpSell: exitLevelsForSell) tpSell.setCore(core);
    }
}
