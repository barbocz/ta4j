package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.num.Num;

public interface BufferedIndicator {

     abstract int bufferSize();

     Num getBufferValue(int bufferIndex, int index);
}
