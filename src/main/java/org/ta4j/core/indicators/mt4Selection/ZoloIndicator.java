package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.AlligatorIndicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.HashMap;

public class ZoloIndicator extends CachedIndicator<Num> {
    HighPriceIndicator high;
    LowPriceIndicator low;
    VolumeIndicator vol;

    private  int  UpPeriod      = 8;       // Up Period
    private final int  UpMode        = 1;        // Up Mode (1: /Period, 2: /Bars)
    private  int  DownPeriod    = 8;       // Down Period
    private final int  DownMode      = 1;        // Down Mode (1: /Period, 2: /Bars)
    private final boolean isUp;  // alapból az up-ot adja


    public ZoloIndicator(TimeSeries series,int upPeriod,int downPeriod,boolean isUp) {
        super(series);
        this.isUp=isUp;
        UpPeriod=upPeriod;
        DownPeriod=downPeriod;
        high=new HighPriceIndicator(series);
        low=new LowPriceIndicator(series);
        vol=new VolumeIndicator(series);
    }

    public ZoloIndicator(TimeSeries series,boolean isUp) {
        super(series);
        this.isUp=isUp;
        high=new HighPriceIndicator(series);
        low=new LowPriceIndicator(series);
        vol=new VolumeIndicator(series);
    }



    @Override
    protected Num calculate(int index) {
        if (index<21) return NaN.NaN;

        double sumUp=0.0,sumDown=0.0;
        for (int i = index ; i > index - UpPeriod ; i--) {
            if( high.getValue(i).isGreaterThan(high.getValue(i-1)) && low.getValue(i).isGreaterThanOrEqual(low.getValue(i-1))) sumUp+=vol.getValue(i).doubleValue();
        }

        for (int i = index ; i > index - DownPeriod ; i--) {
            if( high.getValue(i).isLessThanOrEqual(high.getValue(i-1)) && low.getValue(i).isLessThan(low.getValue(i-1))) sumDown+=vol.getValue(i).doubleValue();
        }

        if (isUp) return numOf(sumUp/UpPeriod);
        else return numOf(sumDown/DownPeriod);

    }



}
/*
    int i = 0;
   if(i<prev_calculated) i=prev_calculated-1;
        for(i=MathMax(i,10);i<rates_total&&!IsStopped();i++){

        bool jump = false;
        if( high.getValue(index)>high.getValue(index-1) && low.getValue(index)>=low.getValue(index-1)){
        upvol.put(index = (double)tick_volume[i];
        downvol.put(index = 0;
        neutralvol.put(index = 0;
        jump = true;
        }
        if( high.getValue(index)>high.getValue(index-1) && low.getValue(index)<low.getValue(index-1)){
        upvol.put(index = 0;
        downvol.put(index = 0;
        neutralvol.put(index = (double)tick_volume[i];
        jump = true;
        }
        if( high.getValue(index)<=high.getValue(index-1) && low.getValue(index)<low.getValue(index-1)){
        upvol.put(index = 0;
        downvol.put(index = (double)tick_volume[i];
        neutralvol.put(index = 0;
        jump = true;
        }
        if( high.getValue(index)<=high.getValue(index-1) && low.getValue(index)>=low.getValue(index-1)){
        upvol.put(index = 0;
        downvol.put(index = 0;
        neutralvol.put(index = 0;
        jump = true;
        }

        _up[i] = 0;
        _down[i] = 0;
        if ( i > 20 + MathMax(UpPeriod,DownPeriod) ){
        // _UP
        int count = 0;
        double sum = 0;
        for ( int j=0; j<UpPeriod; j++ ){
        if ( upvol[i-j] > 0 ){
        sum = sum + upvol[i-j];
        count++;
        }
        }
        int divide = UpPeriod;
        if ( UpMode == 2 ) divide = count;
        if ( divide > 0 ) _up[i] = sum/divide;

        // _down
        count = 0;
        sum = 0;
        for ( int j=0; j<DownPeriod; j++ ){
        if ( downvol[i-j] > 0 ){
        sum = sum + downvol[i-j];
        count++;
        }
        }
        divide = DownPeriod;
        if ( DownMode == 2 ) divide = count;
        if ( divide > 0 ) _down[i] = sum/divide;
        }

        }

        return(rates_total);
        */
