package org.util;


public class PreRuleCheck {
    public static void main(String[] args) {

//        MT4TimeSeries series = new MT4TimeSeries.SeriesBuilder().
//                withName("EURUSD").
//                withPeriod(3).
////                withOhlcvFileName("EURUSD_1.csv").
////                withOhlcvFileName("EURUSD_1_YEAR.csv").
//                withOhlcvFileName("GBPUSD_3_YEAR.csv").
////                withDateFormatPattern("yyyy.MM.dd HH:mm").
//                withDateFormatPattern("dd.MM.yyyy HH:mm").
//                withNumTypeOf(PrecisionNum.class).
//                build();
//
//        Num atrMultiple=series.numOf(2.0);
//
//        ATRIndicator atrIndicator=new ATRIndicator(series,64);
//
//
//        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(series, 20);
//        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM,40,3);
//        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
//
//        MbfxTimingIndicator mbfxTiming=new MbfxTimingIndicator(series,5);
////        System.out.println(mbfxTiming.getValue(4299)+"  "+mbfxTiming.getValue(4300));
//
//        Rule sellRule =new CrossedDownIndicatorRule(mbfxTiming,series.numOf(90.0),5);
//        sellRule = sellRule.and(new CrossedDownIndicatorRule(lowPriceIndicator, kcU,1));
//
////        sellRule = new CrossedDownIndicatorRule(lowPriceIndicator, kcU,3);
//
//        Rule buyRule=new CrossedUpIndicatorRule(mbfxTiming,series.numOf(20.0),1);
//
//        SLTPManager sltpManager=null;
//
//
//        BaseStrategyExtended sellStrategy = new BaseStrategyExtended(Order.OrderType.SELL, sellRule, sellRule,sltpManager,sltpManager);
//        BackTestContext backTestContext=BackTestContext.getInstance();
//        backTestContext.set(series,null,sellStrategy);
//        BaseTradingRecordExtended tradingRecord = new BaseTradingRecordExtended();
//
//        int profitable=0;
//        int lossMaking=0;
//
//        for (int i = 64; i < series.getEndIndex(); i++) {
//            if (sellStrategy.shouldEnter(i, tradingRecord)
//                    ) {
////            System.out.println(mbfxTiming.getValue(i));
//
//                Num takeProfit=series.getBar(i).getClosePrice().minus(atrIndicator.getValue(i).multipliedBy(atrMultiple));
//                Num stopLoss=series.getBar(i).getClosePrice().plus(atrIndicator.getValue(i).multipliedBy(atrMultiple));
//                boolean tpOk=false;
//                boolean slOk=false;
//                int j = i + 1;
//                while (j <= series.getEndIndex()) {
////                    Num lp=series.getBar(j).getLowPrice();
////                    Num hp=series.getBar(j).getHighPrice();
//                    if (series.getBar(j).getLowPrice().isLessThanOrEqual(takeProfit)) tpOk=true;
//                    if (series.getBar(j).getHighPrice().isGreaterThanOrEqual(stopLoss)) slOk=true;
//                    if (tpOk || slOk) break;
//                    j++;
//                }
//                if (tpOk && !slOk) {
////                    System.out.println(i+". Profit "+j);
//                    profitable++;
//                }
//                if (!tpOk && slOk) {
////                    System.out.println(i+". Loss "+j);
//                    lossMaking++;
//                }
////                if (tpOk && slOk) System.out.println(i+". Profit / Loss "+j);
//
//            }
//        }
//
//        System.out.println("profitable: "+profitable+",  lossMaking:"+lossMaking+", ratio:"+((double)profitable*100)/(profitable+lossMaking));

//        System.out.println(mbfxTiming.getValue(4299)+"  "+mbfxTiming.getValue(4300));

    }
}
