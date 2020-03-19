package org;

import org.strategy.TimeSeriesRepo;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.time.ZonedDateTime;
import java.util.*;

public class PreTest {

    final Double murrayRange = 76.24;
    TimeSeriesRepo timeSeriesRepo;
    TimeSeries series, m1Series;
    MoneyFlowIndicator moneyFlowIndicator;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];

    HighPriceIndicator highPriceIndicator;
    HighestValueIndicator highestValueIndicator;

    LowPriceIndicator lowPriceIndicator;
    LowestValueIndicator lowestValueIndicator;

    List<Integer> upperMurrayLevelsToCheck = new ArrayList<Integer>(Arrays.asList(9, 10, 11));
    List<Integer> lowerMurrayLevelsToCheck = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
    List<Double> upperMurrayLevelValues = new ArrayList<>();
    List<Double> lowerMurrayLevelValues = new ArrayList<>();

    HashMap<Double, Integer> downwardLevels = new HashMap<>();
    HashMap<Double, Integer> upwardLevels = new HashMap<>();

    Bar bar;

    HashMap< Integer,Double>  buyEntries=new  HashMap<>();

    public static void main(String[] args) throws Exception {
        PreTest preTest = new PreTest();
    }


    PreTest() {
        timeSeriesRepo = new TimeSeriesRepo("EURUSD", "backtest.csv", "yyyy.MM.dd HH:mm");
        series = timeSeriesRepo.getTimeSeries(3);
        m1Series = timeSeriesRepo.coreSeries;
        moneyFlowIndicator = new MoneyFlowIndicator(series, 3);


//        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(series);
//        HighestValueIndicator highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 4);
//
//        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
//        LowestValueIndicator lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 4);

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(series, i, murrayRange);
        Bar minuteBar;
        double openPrice, highPrice, lowPrice, closePrice;
        int index;
        boolean checkUpper, checkLower;

        for (int i = 0 ; i < timeSeriesRepo.coreSeries.getEndIndex(); i++) {
            minuteBar = timeSeriesRepo.coreSeries.getBar(i);
            ZonedDateTime time = minuteBar.getBeginTime();
            index = series.getIndex(time);

            if (series.getBar(index + 1).getHighPrice().isGreaterThan(murrayMathIndicators[9].getValue(index)))
                checkUpper = true;
            else checkUpper = false;
            if (series.getBar(index + 1).getLowPrice().isLessThan(murrayMathIndicators[3].getValue(index)))
                checkLower = true;
            else checkLower = false;




            if (!checkUpper && !checkLower) continue;

            for(Iterator<Double> iterator = downwardLevels.keySet().iterator(); iterator.hasNext(); ) {
                Double key = iterator.next();
                if(index - downwardLevels.get(key) > 5) {
                    iterator.remove();
                }
            }

            bar = series.getBar(index + 1);


            // tick
            openPrice = minuteBar.getOpenPrice().doubleValue();
            highPrice = minuteBar.getHighPrice().doubleValue();
            lowPrice = minuteBar.getLowPrice().doubleValue();
            closePrice = minuteBar.getClosePrice().doubleValue();


            upperMurrayLevelValues.clear();
            for (Integer murrayLevel : upperMurrayLevelsToCheck)
                upperMurrayLevelValues.add(murrayMathIndicators[murrayLevel].getValue(index).doubleValue());

            lowerMurrayLevelValues.clear();
            for (Integer murrayLevel : lowerMurrayLevelsToCheck)
                lowerMurrayLevelValues.add(murrayMathIndicators[murrayLevel].getValue(index).doubleValue());


//            System.out.println("setCurrentTime: "+simpleDateFormatter.format(time)+",  otime: "+simpleDateFormatter.format(series.getCurrentBar().getBeginTime())+", etime: "+simpleDateFormatter.format(series.getCurrentBar().getEndTime()));
            // barchange

            if (openPrice > closePrice) {
                check(index, time, openPrice, highPrice, checkUpper);
                check(index, time, highPrice, lowPrice, checkUpper);
                check(index, time, lowPrice, closePrice, checkUpper);
            } else {
                check(index, time, openPrice, lowPrice, checkUpper);
                check(index, time, lowPrice, highPrice, checkUpper);
                check(index, time, highPrice, closePrice, checkUpper);
            }
        }

//        for (int i = series.getEndIndex() - 30; i < series.getEndIndex(); i++) {
//            if (series.getBar(i).getParameters().size() > 0) {
//                for (Double v : series.getBar(i).getParameters())
//                    System.out.println(i + ". " + series.getBar(i).getBeginTime() + ": " + v);
//            }

        for(Integer i: buyEntries.keySet()) {
            System.out.println(i + ". " + series.getBar(i).getBeginTime() + ": " + buyEntries.get(i));
        }

//        for (int i = series.getEndIndex() - 100; i < series.getEndIndex(); i++) {
//            if (getBreachLevel(i) > -1) System.out.println(series.getBar(i).getBeginTime());
//        }
    }


    void init() {


    }

    void check(int index, ZonedDateTime time, double prevPrice, double currPrice, boolean checkUpper) {
//        System.out.println(series.getBar(index).getBeginTime() + " - " + time + ": " + prevPrice + ", " + currPrice + "   " + moneyFlowIndicator.getValue(index));
        double murrayValue;

        if (checkUpper) {
            for (Double murrayLevel : upperMurrayLevelValues) {
//                if (prevPrice<murrayLevel && currPrice>murrayLevel ) series.getBar(index).addParameter(murrayLevel);

            }
        } else {
//            if (index>26115) {
//                System.out.println();
//            }

            for (Double murrayLevel : lowerMurrayLevelValues) {
                if (prevPrice > murrayLevel && currPrice < murrayLevel) {
                    if (downwardLevels.containsKey(murrayLevel)) downwardLevels.replace(murrayLevel, index);
                    else downwardLevels.put(murrayLevel, index);
                }
                if (prevPrice < murrayLevel && currPrice > murrayLevel) {
                    int lessCounter=0,moreCounter=0;
                    for(Double downwardLevel: downwardLevels.keySet()) {
                        if (downwardLevel<murrayLevel && index-downwardLevels.get(downwardLevel)<5) lessCounter++;
                        if (downwardLevel>murrayLevel && index-downwardLevels.get(downwardLevel)<5) moreCounter++;
                    }
                    if (lessCounter>0 &&  moreCounter>0 && !buyEntries.containsKey(index)) buyEntries.put(index,murrayLevel);
                }

            }
        }
    }

    boolean isSellOk(int barIndex) {
        return false;
    }

    int getBreachLevel(int barIndex) {
        int breachedLevel = -1;
        double murrayValue;
        for (Integer murrayLevel : upperMurrayLevelsToCheck) {
            murrayValue = murrayMathIndicators[murrayLevel].getValue(barIndex).doubleValue();
            if (series.getBar(barIndex).getHighPrice().doubleValue() > murrayValue &&
                    series.getBar(barIndex).getLowPrice().doubleValue() < murrayValue && series.getBar(barIndex).getOpenPrice().doubleValue() > murrayValue) {
                int index = m1Series.getIndex(series.getBar(barIndex).getBeginTime());
                for (int i = 1; i < 4; i++) {
                    System.out.println(" --- " + m1Series.getBar(index + i).getBeginTime() + " op:" + m1Series.getBar(index + i).getOpenPrice());
                }
                return murrayLevel;
            }
        }
        for (Integer murrayLevel : lowerMurrayLevelsToCheck) {
            murrayValue = murrayMathIndicators[murrayLevel].getValue(barIndex).doubleValue();
            if (series.getBar(barIndex).getHighPrice().doubleValue() > murrayValue && series.getBar(barIndex).getLowPrice().doubleValue() < murrayValue && series.getBar(barIndex).getOpenPrice().doubleValue() < murrayValue)
                return murrayLevel;
        }
        return breachedLevel;
    }


}
