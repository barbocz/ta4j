package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

public class LaguerreIndicator extends CachedIndicator<Num> {



    private final ClosePriceIndicator closePriceIndicator;

    private final double gamma;
    private final int averageBy=3;
    private final int barCount=128;

    public LaguerreIndicator(TimeSeries series, double gamma) {
        super(series);
        closePriceIndicator = new ClosePriceIndicator(series);
        this.gamma=gamma;

    }

    @Override
    protected Num calculate(int index) {
        double L0 = 0;
        double L1 = 0;
        double L2 = 0;
        double L3 = 0;
        double L0A = 0;
        double L1A = 0;
        double L2A = 0;
        double L3A = 0;
        double LRSI = 0;
        double CU = 0;
        double CD = 0;
        double lastThreeSum=0.0;

        for (int i = index - barCount; i <= index; i++) {
            L0A = L0;
            L1A = L1;
            L2A = L2;
            L3A = L3;
            L0 = (1 - gamma)*closePriceIndicator.getValue(i).doubleValue() + gamma*L0A;
            L1 = - gamma *L0 + L0A + gamma *L1A;
            L2 = - gamma *L1 + L1A + gamma *L2A;
            L3 = - gamma *L2 + L2A + gamma *L3A;

            CU = 0;
            CD = 0;

            if (L0 >= L1) CU = L0 - L1; else CD = L1 - L0;
            if (L1 >= L2) CU = CU + L1 - L2; else CD = CD + L2 - L1;
            if (L2 >= L3) CU = CU + L2 - L3; else CD = CD + L3 - L2;

            if (CU + CD != 0) LRSI = CU / (CU + CD);
            if (i>index-averageBy) lastThreeSum+=LRSI;

        }

        return numOf(lastThreeSum/3.0);
    }


}



//+------------------------------------------------------------------+
//|                                                     Laguerre.mq4 |
//|                                                     Emerald King |
//|                                     mailto:info@emerald-king.com |
//+------------------------------------------------------------------+
//#property copyright "Emerald King"
//        #property link      "mailto:info@emerald-king.com"
//
//        #property indicator_separate_window
//        #property indicator_color1 Blue
////---- input parameters
//        extern double gamma=0.5;
//        extern int CountBars=250;
//
//        double L0 = 0;
//        double L1 = 0;
//        double L2 = 0;
//        double L3 = 0;
//        double L0A = 0;
//        double L1A = 0;
//        double L2A = 0;
//        double L3A = 0;
//        double LRSI = 0;
//        double CU = 0;
//        double CD = 0;
//
//        double val1[];
////+------------------------------------------------------------------+
////| Custom indicator initialization function                         |
////+------------------------------------------------------------------+
//        int init()
//        {
////---- indicators
////----
//        SetIndexBuffer(0,val1);
//        return(0);
//        }
////+------------------------------------------------------------------+
////| Custor indicator deinitialization function                       |
////+------------------------------------------------------------------+
//        int deinit()
//        {
////---- TODO: add your code here
//
////----
//        return(0);
//        }
////+------------------------------------------------------------------+
////| Custom indicator iteration function                              |
////+------------------------------------------------------------------+
//        int start()
//        {
//        if (CountBars>Bars) CountBars=Bars;
//        SetIndexDrawBegin(0,Bars-CountBars);
//
//        int i;
//        int    counted_bars=IndicatorCounted();
//
//        //if(CountBars<=Lookback) return(0);
//        //---- initial zero
//        //if(counted_bars<1)
//        //{
//        //   for(i=1;i<=Lookback;i++) val1[CountBars-i]=0.0;
//        //}
//
//        i=CountBars-1;
//        while(i>=0)
//        {
//        L0A = L0;
//        L1A = L1;
//        L2A = L2;
//        L3A = L3;
//        L0 = (1 - gamma)*Close[i] + gamma*L0A;
//        L1 = - gamma *L0 + L0A + gamma *L1A;
//        L2 = - gamma *L1 + L1A + gamma *L2A;
//        L3 = - gamma *L2 + L2A + gamma *L3A;
//
//        CU = 0;
//        CD = 0;
//
//        if (L0 >= L1) CU = L0 - L1; else CD = L1 - L0;
//        if (L1 >= L2) CU = CU + L1 - L2; else CD = CD + L2 - L1;
//        if (L2 >= L3) CU = CU + L2 - L3; else CD = CD + L3 - L2;
//
//        if (CU + CD != 0) LRSI = CU / (CU + CD);
//        val1[i] = LRSI;
//        i--;
//        }
//        return(0);
//        }
////+-----------------------------------------------------------------
