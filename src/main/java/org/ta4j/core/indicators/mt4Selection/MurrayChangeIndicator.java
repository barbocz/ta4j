/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

/**
 *
 */
public class MurrayChangeIndicator extends CachedIndicator<Num> {

    private MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    private final TimeSeries series;

    public MurrayChangeIndicator(TimeSeries series,double murrayRange) {
        super(series);
        this.series = series;
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(series, i, murrayRange);
        }


    }


    @Override
    protected Num calculate(int barIndex) {
        if (barIndex < 257) return NaN.NaN;

        double closePrice, peak = 0;
        int currentMurray, prevMurray = -1;
        int sameLevelCounter = 0, trend = 0;
        int index = barIndex - 1;

//        System.out.println("open: "+series.getBar(index).getOpenPrice().doubleValue(););
        for (int i = 0; i < 13; i++) murrayLevels[i] = murrayMathIndicators[i].getValue(barIndex).doubleValue();

        double murrayHeight = murrayLevels[1] - murrayLevels[0];
        int startMurray = getMurrayRange(series.getBar(barIndex).getOpenPrice().doubleValue());
        while (index > barIndex - 64) {
            closePrice = series.getBar(index).getClosePrice().doubleValue();

//            System.out.println(getMurrayRange( openPrice) + "  " + trend);
            currentMurray = getMurrayRange(closePrice);

            if (trend == 0) {
                if (startMurray != currentMurray) {
                    if (startMurray > currentMurray) {
                        trend = 1; //DOWN
                        peak = series.getBar(index).getHighPrice().doubleValue();
                    } else {
                        trend = 2; // UP   visszafele emelkedik
                        peak = series.getBar(index).getLowPrice().doubleValue();
                    }
                    prevMurray = currentMurray;
                } else sameLevelCounter++;
                if (sameLevelCounter==8) return(series.numOf(0.0));
            }


            if (trend == 2) {  //UP
                if (series.getBar(index).getHighPrice().doubleValue() > peak)
                    peak = series.getBar(index).getHighPrice().doubleValue();

                if (prevMurray > currentMurray) {
                    if (prevMurray - currentMurray > 1) break;
//                    System.out.println(series.getBar(index).getLowPrice().doubleValue());
                    if (peak - murrayHeight > series.getBar(index).getLowPrice().doubleValue()) break;
                }
                if (prevMurray < currentMurray) sameLevelCounter = 0;
                else sameLevelCounter++;
            }
            if (trend == 1) {  // DOWN
                if (series.getBar(index).getLowPrice().doubleValue() < peak)
                    peak = series.getBar(index).getLowPrice().doubleValue();

                if (prevMurray < currentMurray) {
                    if (currentMurray - prevMurray > 1) break;
//                    System.out.println(series.getBar(index).getHighPrice().doubleValue());
                    if (series.getBar(index).getHighPrice().doubleValue() - murrayHeight > peak) break;
                }
                if (prevMurray > currentMurray) sameLevelCounter = 0;
                else sameLevelCounter++;
            }

            if (sameLevelCounter == 8) break;

            prevMurray = currentMurray;
            index--;
        }
//        System.out.println("OK " + (peak - series.getBar(barIndex).getOpenPrice().doubleValue()) / murrayHeight + "   " + series.getBar(index++).getBeginTime());


        return (series.numOf((peak - series.getBar(barIndex).getClosePrice().doubleValue())/murrayHeight));

    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public int getMurrayRange(double value) {
        if (murrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        }

        return -1;
    }


}
