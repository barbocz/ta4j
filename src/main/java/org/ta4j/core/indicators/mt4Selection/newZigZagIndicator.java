package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class newZigZagIndicator extends CachedIndicator<Num> {


    private int depth; //12
    private int deviation; //5
    private int backstep; //3
    private double point; //0.0001~0.01
    private int lookBack = 256;

    // indicator buffers
    private double zigzagBuffer[]; // main buffer
    private double highMapBuffer[]; // highs
    private double lowMapBuffer[]; // lows
    private int level = 3; // recounting depth
    private double deviationInPoints = 0.00005; // deviation in points

    // auxiliary enumeration
    private static final int PIKE = 1; // searching for next high
    private static final int SILL = -1; // searching for next low

    public static enum Type {
        TREND_UP,
        TREND_DOWN
    }


    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private LowestValueIndicator lowestValueIndicator;
    private HighestValueIndicator highestValueIndicator;
    private List<Num> ExtHighBuffer;


    private Type type;

    public newZigZagIndicator(TimeSeries series, Type type) {
        super(series);
        this.type = type;
        highPriceIndicator = new HighPriceIndicator(series);
        lowPriceIndicator = new LowPriceIndicator(series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, depth);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, depth);


//        alance = new ArrayList<>(Collections.singletonList(openBalance));

    }

    @Override
    protected Num calculate(int index) {
        int i = 0;
        int limit = 0, counterZ = 0, whatlookfor = 0;
        int shift = 0, back = 0, lasthighpos = 0, lastlowpos = 0;
        double val = 0, res = 0;
        double curlow = 0, curhigh = 0, lasthigh = 0, lastlow = 0;
        int prev_calculated = 0;


        zigzagBuffer = new double[lookBack];
        highMapBuffer = new double[lookBack];
        lowMapBuffer = new double[lookBack];


//        if (lookBack < 100) {
//            System.out.println("ZigZag.calculate(): Not ebought bars for calculation");
//            //throw new IllegalArgumentException("Not ebought bars for calculation");
//        }

        // set start position for calculations
        if (prev_calculated == 0)
            limit = depth;

        // ZigZag was already counted before
        if (prev_calculated > 0) {
            i = lookBack - 1;

            // searching third extremum from the last uncompleted bar
            while (counterZ < level && i > lookBack - 100) {
                res = zigzagBuffer[i];

                if (res != 0)
                    counterZ++;

                i--;
            }

            i++;
            limit = i;

            // what type of exremum we are going to find
            if (lowMapBuffer[i] != 0) {
                curlow = lowMapBuffer[i];
                whatlookfor = PIKE;
            } else {
                curhigh = highMapBuffer[i];
                whatlookfor = SILL;
            }

            // chipping
            for (i = limit + 1; i < lookBack; i++) {
                zigzagBuffer[i] = 0.0;
                lowMapBuffer[i] = 0.0;
                highMapBuffer[i] = 0.0;
            }
        }

        // searching High and Low
        for (shift = limit; shift < lookBack; shift++) {
            val = lowestValueIndicator.getValue(shift).doubleValue();

            if (val == lastlow)
                val = 0.0;
            else {
                lastlow = val;

                if ((lowPriceIndicator.getValue(shift).doubleValue() - val) > deviationInPoints)
                    val = 0.0;
                else {
                    for (back = 1; back <= backstep; back++) {
                        res = lowMapBuffer[shift - back];

                        if ((res != 0) && (res > val))
                            lowMapBuffer[shift - back] = 0.0;
                    }
                }
            }

            if (lowestValueIndicator.getValue(shift).doubleValue() == val)
                lowMapBuffer[shift] = val;
            else
                lowMapBuffer[shift] = 0.0;

            // high
            val = highestValueIndicator.getValue(shift).doubleValue();

            if (val == lasthigh)
                val = 0.0;
            else {
                lasthigh = val;

                if ((val - highPriceIndicator.getValue(shift).doubleValue()) > deviationInPoints)
                    val = 0.0;
                else {
                    for (back = 1; back <= backstep; back++) {
                        res = highMapBuffer[shift - back];

                        if ((res != 0) && (res < val))
                            highMapBuffer[shift - back] = 0.0;
                    }
                }
            }

            if (highPriceIndicator.getValue(shift).doubleValue() == val)
                highMapBuffer[shift] = val;
            else
                highMapBuffer[shift] = 0.0;
        }

        // last preparation
        if (whatlookfor == 0) // uncertain quantity
        {
            lastlow = 0;
            lasthigh = 0;
        } else {
            lastlow = curlow;
            lasthigh = curhigh;
        }

        // final rejection
        for (shift = limit; shift < lookBack; shift++) {
            res = 0.0;

            switch (whatlookfor) {
                case 0: // search for peak or lawn
                    if (lastlow == 0 && lasthigh == 0) {
                        if (highMapBuffer[shift] != 0) {
                            lasthigh = highPriceIndicator.getValue(shift).doubleValue();
                            lasthighpos = shift;
                            whatlookfor = SILL;
                            zigzagBuffer[shift] = lasthigh;
                            res = 1;
                        }

                        if (lowMapBuffer[shift] != 0) {
                            lastlow = lowPriceIndicator.getValue(shift).doubleValue();
                            lastlowpos = shift;
                            whatlookfor = PIKE;
                            zigzagBuffer[shift] = lastlow;
                            res = 1;
                        }
                    }

                    break;

                case PIKE: // search for peak
                    if (lowMapBuffer[shift] != 0.0 && lowMapBuffer[shift] < lastlow && highMapBuffer[shift] == 0.0) {
                        zigzagBuffer[lastlowpos] = 0.0;
                        lastlowpos = shift;
                        lastlow = lowMapBuffer[shift];
                        zigzagBuffer[shift] = lastlow;
                        res = 1;
                    }

                    if (highMapBuffer[shift] != 0.0 && lowMapBuffer[shift] == 0.0) {
                        lasthigh = highMapBuffer[shift];
                        lasthighpos = shift;
                        zigzagBuffer[shift] = lasthigh;
                        whatlookfor = SILL;
                        res = 1;
                    }

                    break;

                case SILL: // search for lawn
                    if (highMapBuffer[shift] != 0.0 && highMapBuffer[shift] > lasthigh && lowMapBuffer[shift] == 0.0) {
                        zigzagBuffer[lasthighpos] = 0.0;
                        lasthighpos = shift;
                        lasthigh = highMapBuffer[shift];
                        zigzagBuffer[shift] = lasthigh;
                    }

                    if (lowMapBuffer[shift] != 0.0 && highMapBuffer[shift] == 0.0) {
                        lastlow = lowMapBuffer[shift];
                        lastlowpos = shift;
                        zigzagBuffer[shift] = lastlow;
                        whatlookfor = PIKE;
                    }

                    break;
            }
        }

        if (type == Type.TREND_UP) return DoubleNum.valueOf(highMapBuffer[index]);
        if (type == Type.TREND_DOWN) return DoubleNum.valueOf(lowMapBuffer[index]);

        return NaN.NaN;

    }


}


