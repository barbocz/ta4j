package org.strategy;


import org.strategy.entryAndExit.KeltnerEdgeVer1_2;
import org.ta4j.core.Bar;
import org.ta4j.core.extendedCore.BaseTradingRecordExtended;

public class StrategyTester {
    public static void main(String[] args) {

//        BackTestContext backTestContext = BackTestContext.getInstance();
//        backTestContext.set(new Entry(), new Exit(), new StopLossTakeProfit(), new IndicatorSet(),true);
//
//
//        TimeSeriesManagerExtended seriesManager = new TimeSeriesManagerExtended(backTestContext);
////        backTestContext.setDebug(true);

        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD_1_TEST.csv");
        KeltnerEdgeVer1_2 strategy=new KeltnerEdgeVer1_2(timeSeriesRepo,3);

        BaseTradingRecordExtended record = new BaseTradingRecordExtended(strategy);

        for (int i = 0; i < 8; i++) {
            Bar bar= timeSeriesRepo.timeSeries.get(1440).getBar(i);
            System.out.println(i+". "+bar.getEndTime()+":  "+bar.getOpenPrice()+", "+bar.getHighPrice()+", "+bar.getLowPrice()+", "+bar.getClosePrice()+", "+bar.getVolume());
        }

        record.setDebug(true);

        record.runBackTest();


////
        record.getLastStrategyResults(4);
//        System.out.println("Trade size: "+record.tradeSize+", profitable: "+record.profitableTradesRatio+", balance dd: "+record.balanceDrawDown.formatToString(2)+", equity dd: "+record.equityDrawDown.formatToString(2)+", total profit/month: "+record.totalProfitPerMonth);

    }

}
