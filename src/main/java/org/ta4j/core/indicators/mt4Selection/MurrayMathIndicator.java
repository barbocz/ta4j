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

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for price indicators
 */
public class MurrayMathIndicator extends CachedIndicator<Num> implements BufferedIndicator{

    private final int murrayPeriod;
    private final LowPriceIndicator lowPriceIndicator;
    private final LowestValueIndicator lowestValueIndicator;
    private final HighPriceIndicator highPriceIndicator;
    private final HighestValueIndicator highestValueIndicator;
    private Num[] buffer;
    private final int level;


    public MurrayMathIndicator(TimeSeries series, int murrayPeriod,int level) {
        super(series);
        this.murrayPeriod = murrayPeriod;
        this.level=level;
        lowPriceIndicator=new LowPriceIndicator(series);
        lowestValueIndicator=new LowestValueIndicator(lowPriceIndicator,murrayPeriod);
        highPriceIndicator=new HighPriceIndicator(series);
        highestValueIndicator=new HighestValueIndicator(highPriceIndicator,murrayPeriod);
        buffer =new Num[13];

    }

    @Override
    public int bufferSize(){
        return buffer.length;
    }

    @Override
    public Num getBufferValue(int bufferIndex,int index){
        final Bar bar = getTimeSeries().getBar(index);
        calculate(index);
        return buffer[bufferIndex];
    }


    public List<Num> getBufferValues( int index){
        calculate(index);
        return Arrays.asList(buffer) ;
    }

    @Override
    protected Num calculate(int index) {

        double  dmml=0,
                dvtl = 0,
                sum  = 0,
                v1 = 0,
                v2 = 0,
                mn = 0,
                mx = 0,
                x1 = 0,
                x2 = 0,
                x3 = 0,
                x4 = 0,
                x5 = 0,
                x6 = 0,
                y1 = 0,
                y2 = 0,
                y3 = 0,
                y4 = 0,
                y5 = 0,
                y6 = 0,
                octave=0,
                fractal = 0,
                range   = 0,
                finalH  = 0,
                finalL  = 0;

        double mml[]=new double[13];



         v1 = lowestValueIndicator.getValue(index).doubleValue();;
         v2 = highestValueIndicator.getValue(index).doubleValue();


//determine fractal.....

        if(v2<=1.5625 && v2>0.390625)
            fractal=1.5625;
        else if(v2<=0.390625 && v2>0)
            fractal=0.1953125;
        else if(v2<=3.125 && v2>1.5625)
            fractal=3.125;
        else if(v2<=6.25 && v2>3.125)
            fractal=6.25;
        else if(v2<=250000 && v2>25000)
            fractal=100000;
        else if(v2<=25000 && v2>2500)
            fractal=10000;
        else if(v2<=2500 && v2>250)
            fractal=1000;
        else if(v2<=250 && v2>25)
            fractal=100;
        else if(v2<=25 && v2>12.5)
            fractal=12.5;
        else  if(v2<=12.5 && v2>6.25)
            fractal=12.5;

        range=(v2-v1);
        if(range==0) return NaN.NaN;

        sum=Math.floor(Math.log(fractal/range)/Math.log(2));
        octave=fractal*(Math.pow(0.5,sum));

        if(octave==0) return NaN.NaN;

        mn=Math.floor(v1/octave)*octave;
        if((mn+octave)>v2) mx=mn+octave;   else   mx=mn+(2*octave);

// calculating xx

        if((v1>=(3*(mx-mn)/16+mn)) && (v2<=(9*(mx-mn)/16+mn))) x2=mn+(mx-mn)/2;     else x2=0;

        if((v1>=(mn-(mx-mn)/8)) && (v2<=(5*(mx-mn)/8+mn)) && (x2==0)) x1=mn+(mx-mn)/2; else x1=0;

        if((v1>=(mn+7*(mx-mn)/16)) && (v2<=(13*(mx-mn)/16+mn))) x4=mn+3*(mx-mn)/4;     else x4=0;

        if((v1>=(mn+3*(mx-mn)/8)) && (v2<=(9*(mx-mn)/8+mn)) && (x4==0)) x5=mx;   else  x5=0;


        if((v1>=(mn+(mx-mn)/8)) && (v2<=(7*(mx-mn)/8+mn)) && (x1==0) && (x2==0) && (x4==0) && (x5==0)) x3=mn+3*(mx-mn)/4; else x3=0;

        if((x1+x2+x3+x4+x5)==0) x6=mx;  else x6=0;

        finalH=x1+x2+x3+x4+x5+x6;


// calculating yy
        if(x1>0) y1=mn;  else y1=0;

        if(x2>0) y2=mn+(mx-mn)/4; else y2=0;

        if(x3>0) y3=mn+(mx-mn)/4; else y3=0;

        if(x4>0) y4=mn+(mx-mn)/2; else y4=0;

        if(x5>0) y5=mn+(mx-mn)/2; else y5=0;

        if((finalH>0) && ((y1+y2+y3+y4+y5)==0)) y6=mn;    else y6=0;



        finalL=y1+y2+y3+y4+y5+y6;

        dmml=(finalH-finalL)/8;
        buffer[0]=numOf(finalL-dmml*2); //-2/8

//        if (index==8990) {
//            System.out.println("break");
//        }

        for (int i = 1; i < 13; i++) buffer[i]=buffer[i-1].plus(numOf(dmml));

        return buffer[level];
    }


    protected Num calculate1(int index) {
//        Num maxPrice = getTimeSeries().getBar(index).getHighPrice();
        Num fractal=numOf(0.0);
        Num v1=lowestValueIndicator.getValue(index);
        Num v2=highestValueIndicator.getValue(index);


        if (v2.isLessThanOrEqual(numOf(0.390625)) && v2.isGreaterThan(numOf(0))) fractal=numOf(0.1953125);
        else if (v2.isLessThanOrEqual(numOf(1.5625)) && v2.isGreaterThan(numOf(0.390625))) fractal=numOf(1.5625);
        else if (v2.isLessThanOrEqual(numOf(3.125)) && v2.isGreaterThan(numOf(1.5625))) fractal=numOf(3.125);
        else if (v2.isLessThanOrEqual(numOf(6.25)) && v2.isGreaterThan(numOf(3.125))) fractal=numOf(6.25);
        else if (v2.isLessThanOrEqual(numOf(12.5)) && v2.isGreaterThan(numOf(6.25))) fractal=numOf(12.5);
        else if (v2.isLessThanOrEqual(numOf(25)) && v2.isGreaterThan(numOf(12.5))) fractal=numOf(12.5);
        else if (v2.isLessThanOrEqual(numOf(250)) && v2.isGreaterThan(numOf(25))) fractal=numOf(100);
        else if (v2.isLessThanOrEqual(numOf(2500)) && v2.isGreaterThan(numOf(250))) fractal=numOf(1000);
        else if (v2.isLessThanOrEqual(numOf(25000)) && v2.isGreaterThan(numOf(2500))) fractal=numOf(10000);
        else if (v2.isLessThanOrEqual(numOf(250000)) && v2.isGreaterThan(numOf(25000))) fractal=numOf(100000);

        Num range=v2.minus(v1);
        if (range.isEqual(numOf(0))) return NaN.NaN;
        Num sum=fractal.dividedBy(range).log().dividedBy(numOf(2).log()).floor();

        Num octave=fractal.multipliedBy(numOf(0.5).pow(sum));

        if (octave.isEqual(numOf(0))) return NaN.NaN;

        Num mn=v1.dividedBy(octave).floor().multipliedBy(octave);

        Num mx=null,x2,x1,x4,x5,x3,x6;
        if(mn.plus(octave).isGreaterThan(v2)) mx=mn.plus(octave);   else   mx=mn.plus(octave.multipliedBy(numOf(2)));


        if (v1.isGreaterThanOrEqual(mx.minus(mn).multipliedBy(numOf(3).dividedBy(numOf(16))).plus(mn)) &&
        v2.isLessThanOrEqual(mx.minus(mn).multipliedBy(numOf(9)).dividedBy(numOf(16)).plus(mn))) x2=mn.plus(mx.minus(mn).dividedBy(numOf(2)));
        else x2=numOf(0);

        if (v1.isGreaterThanOrEqual(mn.minus(mx.minus(mn).dividedBy(numOf(8)))) &&
                v2.isLessThanOrEqual(mx.minus(mn).multipliedBy(numOf(5).dividedBy(numOf(8))).plus(mn))  &&
                x2.isEqual(numOf(0)))  x1=mn.plus(mx.minus(mn).dividedBy(numOf(2)));
        else x1=numOf(0);

        if (v1.isGreaterThanOrEqual(mn.plus(numOf(7).multipliedBy(mx.minus(mn)).dividedBy(numOf(16)))) &&
                v2.isLessThanOrEqual(mx.minus(mn).multipliedBy(numOf(13)).dividedBy(numOf(16)).plus(mn)))
            x4=mn.plus(numOf(3).multipliedBy(mx.minus(mn)).dividedBy(numOf(4)));
        else x4=numOf(0);

//        if((v1>=(mn+3*(mx-mn)/8)) && (v2<=(9*(mx-mn)/8+mn)) && (x4==0))
        Num n1=mn.plus(numOf(3).multipliedBy(mx.minus(mn)).dividedBy(numOf(8)));
        Num n2=(numOf(9).multipliedBy(mx.minus(mn)).dividedBy(numOf(8))).plus(mn);
        if (v1.isGreaterThanOrEqual(mn.plus(numOf(3).multipliedBy(mx.minus(mn)).dividedBy(numOf(8)))) &&
                v2.isLessThanOrEqual((numOf(9).multipliedBy(mx.minus(mn)).dividedBy(numOf(8))).plus(mn)) && x4.isEqual(numOf(0)))
            x5=mx;
        else x5=numOf(0);

        if (v1.isGreaterThanOrEqual(mn.plus(mx.minus(mn)).dividedBy(numOf(8))) &&
                v2.isLessThanOrEqual(mx.minus(mn).multipliedBy(numOf(7)).dividedBy(numOf(8)).plus(mn))
        && x1.isEqual(numOf(0)) && x2.isEqual(numOf(0)) && x4.isEqual(numOf(0)) && x5.isEqual(numOf(0)))
            x3=mn.plus(numOf(3).multipliedBy(mx.minus(mn)).dividedBy(numOf(4)));
        else x3=numOf(0);

        if (x1.plus(x2).plus(x3).plus(x4).plus(x5).isEqual(numOf(0))) x6=mx; else x6=numOf(0);

        Num finalH=x1.plus(x2).plus(x3).plus(x4).plus(x5).plus(x6);

        Num y1,y2,y3,y4,y5,y6;

        if (x1.isGreaterThan(numOf(0))) y1=mn; else y1=numOf(0);
        if (x2.isGreaterThan(numOf(0))) y2=mn.plus(mx.minus(mn).dividedBy(numOf(4))); else y2=numOf(0);
        if (x3.isGreaterThan(numOf(0))) y3=mn.plus(mx.minus(mn).dividedBy(numOf(4))); else y3=numOf(0);
        if (x4.isGreaterThan(numOf(0))) y4=mn.plus(mx.minus(mn).dividedBy(numOf(2))); else y4=numOf(0);
        if (x5.isGreaterThan(numOf(0))) y5=mn.plus(mx.minus(mn).dividedBy(numOf(2))); else y5=numOf(0);

        if (finalH.isGreaterThan(numOf(0)) && y1.plus(y2).plus(y3).plus(y4).plus(y5).isEqual(numOf(0))) y6=mn; else y6=numOf(0);

        Num finalL=y1.plus(y2).plus(y3).plus(y4).plus(y5).plus(y6);

        Arrays.fill(buffer,numOf(0));
        Num dmml=finalH.minus(finalL).dividedBy(numOf(8));
        buffer[0]=finalL.minus(dmml.multipliedBy(numOf(2)));
        for (int i = 1; i < 13; i++) buffer[i]= buffer[i-1].plus(dmml);

//        System.out.println(buffer[6].toString());
        return buffer[level];
    }


}
