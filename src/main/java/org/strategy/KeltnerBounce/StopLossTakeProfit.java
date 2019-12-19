package org.strategy.KeltnerBounce;

import org.ta4j.core.trading.SLTPManager;
import org.ta4j.core.trading.sltp.BreakEventAfter;
import org.ta4j.core.trading.sltp.LossLimit;
import org.ta4j.core.trading.sltp.IndicatorValue;

import java.util.ArrayList;
import java.util.List;

public class StopLossTakeProfit extends IndicatorSet{

    public  StopLossTakeProfit() {
//        SLTPManager stopLossManager = new BreakEventAfter(4);
        SLTPManager lossLimit = new LossLimit(-100.0);
        List<SLTPManager> stopLossManagers = new ArrayList<>();

        stopLossManagers.add( new BreakEventAfter(4));
        stopLossManagers.add(new IndicatorValue(kcM,5));
//        stopLossManagers.add(lossLimit);

        sellStopLoss=stopLossManagers;
        buyStopLoss=stopLossManagers;

//        SLTPManager takeProfitAtKcmForSell = new CrossedIndicatorValue(kcL, lowPriceIndicator, kcL);
        SLTPManager takeProfitAtKcmForSell = new IndicatorValue(kcL);
        List<SLTPManager> takeProfitManagersForSell = new ArrayList<>();
        takeProfitManagersForSell.add(takeProfitAtKcmForSell);
//        takeProfitManagersForSell.add(new IndicatorValue(kcM,5));

        sellTakeProfit=takeProfitManagersForSell;

//        SLTPManager takeProfitAtKcmForBuy = new CrossedIndicatorValue(highPriceIndicator,kcU, kcU);
        SLTPManager takeProfitAtKcmForBuy = new IndicatorValue(kcU);
        List<SLTPManager> takeProfitManagersForBuy = new ArrayList<>();
        takeProfitManagersForBuy.add(takeProfitAtKcmForBuy);
//        takeProfitManagersForBuy.add(new IndicatorValue(kcM,5));

        buyTakeProfit=takeProfitManagersForBuy;

    }



}
