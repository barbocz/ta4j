package org.strategy.myEntryStrategies;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;


public class Murray3 extends Strategy {
    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];
    Double murrayLevels[] = new Double[13];
    Double prevMurrayLevels[] = new Double[13];
    Double prevBarMurrayLevels[] = new Double[13];
    LowPriceIndicator lowPriceIndicator;
    HighPriceIndicator highPriceIndicator;
    double sellUpperLimit=Double.MAX_VALUE,sellLowerLimit=0.0;
    double buyUpperLimit=Double.MAX_VALUE,buyLowerLimit=0.0;

    int upperMurrayLevel,lowerMurrayLevel,murrayDelta;
    double upperLimit=Double.MAX_VALUE,lowerLimit=0.0;
    ClosePriceIndicator closePriceIndicator;

    int currentMurrayLevel,prevMurrayLevel;


    double murrayHeight=0.0;

    boolean buyOk=false,sellOk=false;
    MultiKeyMap matrix = new MultiKeyMap();


    public void init() {


        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + "matrix.csv"));

//        List<String[]> result = new ArrayList<>();
            String[] result;
            String line="";

            while ((line = bufferedReader.readLine()) != null) {
                result=line.split(",");
                matrix.put(Integer.parseInt(result[0]), Integer.parseInt(result[1]), Integer.parseInt(result[2]), Integer.parseInt(result[3]));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        lowPriceIndicator=new LowPriceIndicator(tradeEngine.series);
        highPriceIndicator=new HighPriceIndicator(tradeEngine.series);

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 256, i);

        Arrays.fill(murrayLevels, 0.0);
        Arrays.fill(prevMurrayLevels, 0.0);
        Arrays.fill(prevBarMurrayLevels, 0.0);

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 9) murrayMathIndicators[i].indicatorColor = Color.YELLOW;
            if (i == 8) murrayMathIndicators[i].indicatorColor = Color.RED;
            if (i == 6) murrayMathIndicators[i].indicatorColor = Color.BLUE;
            if (i == 4) murrayMathIndicators[i].indicatorColor = Color.RED;
            if (i == 3) murrayMathIndicators[i].indicatorColor = Color.YELLOW;
            tradeEngine.log(murrayMathIndicators[i]);
        }



        MoneyFlowIndicator moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);
        moneyFlowIndicator.subWindowIndex = 5;
        tradeEngine.log(moneyFlowIndicator);

//        ruleForBuy=new UnderIndicatorRule(moneyFlowIndicator,1.0,2);
//        ruleForSell=new OverIndicatorRule(moneyFlowIndicator,99.0,2);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
//        longCci.subWindowIndex = 4;
//        tradeEngine.log(longCci);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
        if (tradeEngine.timeSeriesRepo.ask>upperLimit) {
            upperLimit=Double.MAX_VALUE;
            prevMurrayLevel=getMurrayLevel(murrayLevels[upperMurrayLevel],prevMurrayLevels);
            murrayDelta=getMurrayDelta(upperMurrayLevel);
            System.out.println("CROSS UP: "+tradeEngine.series.getCurrentTime()+", ml: "+upperMurrayLevel+", pml: "+prevMurrayLevel+", md: "+murrayDelta);
            if (upperMurrayLevel<12) {
                upperMurrayLevel++;
                upperLimit=murrayLevels[upperMurrayLevel];
            }


        }
        if (tradeEngine.timeSeriesRepo.bid<lowerLimit) {
            lowerLimit=0.0;
            prevMurrayLevel=getMurrayLevel(murrayLevels[lowerMurrayLevel],prevMurrayLevels);
            murrayDelta=getMurrayDelta(lowerMurrayLevel);
            System.out.println("CROSS DN: "+tradeEngine.series.getCurrentTime()+", ml: "+lowerMurrayLevel+", pml: "+prevMurrayLevel+", md: "+murrayDelta);
            if (lowerMurrayLevel>0) {
                lowerMurrayLevel--;
                lowerLimit=murrayLevels[lowerMurrayLevel];
            }

        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        ZonedDateTime time = tradeEngine.series.getCurrentTime();

        for (int m = 0; m < 13; m++)
            murrayLevels[m] = murrayMathIndicators[m].getValue(tradeEngine.currentBarIndex).doubleValue();

        murrayHeight = murrayLevels[1] - murrayLevels[0];
        double closePrice=closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();

        currentMurrayLevel = getMurrayLevel(closePrice,murrayLevels);
        prevMurrayLevel = getMurrayLevel(closePrice,prevBarMurrayLevels);

        if (prevMurrayLevel == -1 || prevMurrayLevel == 12 || !prevBarMurrayLevels[currentMurrayLevel].equals(murrayLevels[currentMurrayLevel]) ||
                !prevBarMurrayLevels[currentMurrayLevel + 1].equals(murrayLevels[currentMurrayLevel + 1])) {
//            PREV:10, ENTRY:6, DELTA:4, EXIT:1
//            if (prevMurrayLevel==10 && currentMurrayLevel==6 && getMurrayDelta(6)==4) {
//                buyLowerLimit=murrayLevels[6];
//            }
//            System.out.println(time);
            prevMurrayLevels = Arrays.copyOf(prevBarMurrayLevels, 13);
            upperMurrayLevel=currentMurrayLevel+1;
            upperLimit=murrayLevels[upperMurrayLevel];
            lowerMurrayLevel=currentMurrayLevel;
            lowerLimit=murrayLevels[lowerMurrayLevel];


//            setEntryLevels();
//            chekEntry(i);
        }

        prevBarMurrayLevels = Arrays.copyOf(murrayLevels, 13);


    }


    public void onOneMinuteDataEvent() {

    }

    int getMurrayLevel(double value,Double[] murrayLevels) {
        if (murrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
            return 12;
        }
        return -1;
    }



    int getMurrayDelta(int entryLevel) {

        double murrayHeight = prevMurrayLevels[1] - prevMurrayLevels[0] > murrayLevels[1] - murrayLevels[0] ? murrayLevels[1] - murrayLevels[0] : prevMurrayLevels[1] - prevMurrayLevels[0];

        return (int) ((murrayLevels[entryLevel] - prevMurrayLevels[entryLevel]) / murrayHeight);

    }


}