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
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for price indicators
 *
 * csak a 2-es (LOWER) és 10-es (UPPER) szinteket számolja
 * murrayPeriod --nak veszi a 64,128,256,512 értékeket
 * csak azokat a murrayPeriod-okat veszi figyelembe ahol a murray height>=38 és <=76
 * ha több period murray height-ja teljesülés akkor azok közül csak az marad ami legalább egy felsőbb period-dal egyezik
 * és igaz rá hogy az összes period 6-os szintje alatt van (LOWER) vagy felett (UPPER) esetén
 * ha ezek után is több marad akkor az alsóbb period élvez prioritást
 */
public class MurrayMathMultiIndicator extends CachedIndicator<Num> {


    private HashMap<Integer, HighestValueIndicator> highestIndicators=new HashMap<>();
    private HashMap<Integer, LowestValueIndicator> lowestIndicators=new HashMap<>();

    Set<Integer> murrayPeriods =  new HashSet<>(Arrays.asList(64,128,256,512));
//    List<Integer> murrayPeriods = Arrays.asList(128);
    private final LevelType levelType;


    public enum LevelType {
        HIGH,
        LOW,
        EXTREME_HIGH,
        EXTREME_LOW
    }


    public MurrayMathMultiIndicator(TimeSeries series, LevelType levelType) {
        super(series);
        this.levelType=levelType;
        for (Integer murrayPeriod: murrayPeriods ){
            highestIndicators.put(murrayPeriod, new HighestValueIndicator(new HighPriceIndicator(series),murrayPeriod));
            lowestIndicators.put(murrayPeriod, new LowestValueIndicator(new LowPriceIndicator(series),murrayPeriod));
        }



    }


    @Override
    protected Num calculate(int index) {

        if (index<512) return NaN.NaN;

//        if (index==3785) {
//            System.out.println("break");
//        }

        HashMap <Integer,MurrayCalculated> murrayCalculatedValues=new HashMap<>();
        for(Integer murrayPeriod: murrayPeriods) {
            murrayCalculatedValues.put(murrayPeriod,preCalculate(lowestIndicators.get(murrayPeriod).getValue(index).doubleValue(),highestIndicators.get(murrayPeriod).getValue(index).doubleValue()));
        }

        // murray height 38 pip?
        for (MurrayCalculated murrayCalculated: murrayCalculatedValues.values()){
            if (murrayCalculated.height>0.00038 && murrayCalculated.height<0.00039)  murrayCalculated.isSatisfied=true;
        }

        // ha murray height nem 38 pip akkor 76?
        for (MurrayCalculated murrayCalculated: murrayCalculatedValues.values()){
            if (!murrayCalculated.isSatisfied && murrayCalculated.height>0.00076 && murrayCalculated.height<0.00077)  murrayCalculated.isSatisfied=true;
        }

        // 64-es level csak akkor lesz kó ha van ismétlődés a felsőbb timeFrame-eken?
        if (murrayCalculatedValues.get(64).isSatisfied && murrayCalculatedValues.get(64).level2!=murrayCalculatedValues.get(128).level2) murrayCalculatedValues.get(64).isSatisfied=false;
        int nextMurrayPeriod;
//        boolean isLevelRepeated;
//
//        for (Integer murrayPeriod: murrayPeriods) {
//            isLevelRepeated=false;
//            nextMurrayPeriod=murrayPeriod*2;
//            while (murrayCalculatedValues.containsKey(nextMurrayPeriod)) {
//                if (murrayCalculatedValues.get(murrayPeriod).level2==murrayCalculatedValues.get(nextMurrayPeriod).level2) {
//                    isLevelRepeated=true;
//                    break;
//                }
//                nextMurrayPeriod=nextMurrayPeriod*2;
//            }
//            if (murrayCalculatedValues.get(murrayPeriod).isSatisfied && !isLevelRepeated) murrayCalculatedValues.get(murrayPeriod).isSatisfied=false;
//        }

        // mindegy timeFrame-en a 10-es level a felső részben található?
        boolean isTenLevelAboveSix;
        for (Integer murrayPeriod: murrayPeriods) {
            if (!murrayCalculatedValues.get(murrayPeriod).isSatisfied) continue;
            isTenLevelAboveSix=true;
            nextMurrayPeriod=murrayPeriod*2;
            while (murrayCalculatedValues.containsKey(nextMurrayPeriod)) {
                if (murrayCalculatedValues.get(murrayPeriod).level10<murrayCalculatedValues.get(nextMurrayPeriod).level6) {
                    isTenLevelAboveSix=false;
                    break;
                }
                nextMurrayPeriod=nextMurrayPeriod*2;
            }
            if (murrayCalculatedValues.get(murrayPeriod).isSatisfied && !isTenLevelAboveSix) murrayCalculatedValues.get(murrayPeriod).isSatisfied=false;
        }

        for (Integer murrayPeriod: murrayPeriods) {
            if ( murrayCalculatedValues.get(murrayPeriod).isSatisfied) {
                if (levelType == LevelType.HIGH) return numOf(murrayCalculatedValues.get(murrayPeriod).level10);
                else if (levelType == LevelType.LOW) return numOf(murrayCalculatedValues.get(murrayPeriod).level2);
                else if (levelType == LevelType.EXTREME_HIGH) return numOf(murrayCalculatedValues.get(murrayPeriod).level12);
                else if (levelType == LevelType.EXTREME_LOW) return numOf(murrayCalculatedValues.get(murrayPeriod).level0);
            }
        }


//        else if (levelType==LevelType.MIDDLE) return numOf(murrayCalculated.level6);

        return NaN.NaN;

    }

    MurrayCalculated preCalculate(double lowestValue,double highestValue) {
        MurrayCalculated murrayCalculated=new MurrayCalculated();

        double mx,fractal=0.0,x1=0.0,x2=0.0,x3=0.0,x4=0.0,x5=0.0,x6=0.0,y1=0.0,y2=0.0,y3=0.0,y4=0.0,y5=0.0,y6=0.0;
        if(highestValue<=1.5625 && highestValue>0.390625)
            fractal=1.5625;
        else if(highestValue<=0.390625 && highestValue>0)
            fractal=0.1953125;
        else if(highestValue<=3.125 && highestValue>1.5625)
            fractal=3.125;
        else if(highestValue<=6.25 && highestValue>3.125)
            fractal=6.25;
        else if(highestValue<=250000 && highestValue>25000)
            fractal=100000;
        else if(highestValue<=25000 && highestValue>2500)
            fractal=10000;
        else if(highestValue<=2500 && highestValue>250)
            fractal=1000;
        else if(highestValue<=250 && highestValue>25)
            fractal=100;
        else if(highestValue<=25 && highestValue>12.5)
            fractal=12.5;
        else  if(highestValue<=12.5 && highestValue>6.25)
            fractal=12.5;

        double range=(highestValue-lowestValue);
        if (range==0) return murrayCalculated;

        double sum=Math.floor(Math.log(fractal/range)/Math.log(2));
        double octave=fractal*(Math.pow(0.5,sum));

        if(octave==0) return murrayCalculated;

        double mn=Math.floor(lowestValue/octave)*octave;
        if((mn+octave)>highestValue) mx=mn+octave;   else   mx=mn+(2*octave);

// calculating xx

        if((lowestValue>=(3*(mx-mn)/16+mn)) && (highestValue<=(9*(mx-mn)/16+mn))) x2=mn+(mx-mn)/2;     else x2=0;

        if((lowestValue>=(mn-(mx-mn)/8)) && (highestValue<=(5*(mx-mn)/8+mn)) && (x2==0)) x1=mn+(mx-mn)/2; else x1=0;

        if((lowestValue>=(mn+7*(mx-mn)/16)) && (highestValue<=(13*(mx-mn)/16+mn))) x4=mn+3*(mx-mn)/4;     else x4=0;

        if((lowestValue>=(mn+3*(mx-mn)/8)) && (highestValue<=(9*(mx-mn)/8+mn)) && (x4==0)) x5=mx;   else  x5=0;


        if((lowestValue>=(mn+(mx-mn)/8)) && (highestValue<=(7*(mx-mn)/8+mn)) && (x1==0) && (x2==0) && (x4==0) && (x5==0)) x3=mn+3*(mx-mn)/4; else x3=0;

        if((x1+x2+x3+x4+x5)==0) x6=mx;  else x6=0;

        double finalH=x1+x2+x3+x4+x5+x6;


// calculating yy
        if(x1>0) y1=mn;  else y1=0;

        if(x2>0) y2=mn+(mx-mn)/4; else y2=0;

        if(x3>0) y3=mn+(mx-mn)/4; else y3=0;

        if(x4>0) y4=mn+(mx-mn)/2; else y4=0;

        if(x5>0) y5=mn+(mx-mn)/2; else y5=0;

        if((finalH>0) && ((y1+y2+y3+y4+y5)==0)) y6=mn;    else y6=0;



        double finalL=y1+y2+y3+y4+y5+y6;

        double dmml=(finalH-finalL)/8;
//        double level0=finalL-dmml*2; //-2/8

        murrayCalculated.level0=finalL-dmml*2;
        murrayCalculated.level2=finalL;
        murrayCalculated.level10=finalH;
        murrayCalculated.level6=finalL+dmml*4.0;
        murrayCalculated.level12=finalL+dmml*10.0;
        murrayCalculated.height=dmml;

        return  murrayCalculated;
    }
    


    class MurrayCalculated{
        double level0,level2,level10,level12,level6,height=0.0;
        boolean isSatisfied=false;
    }


}
