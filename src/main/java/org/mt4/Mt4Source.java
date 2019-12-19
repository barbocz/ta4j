package org.mt4;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.opencsv.CSVReader;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DoubleNum;

public class Mt4Source {

    public String symbol = null;
    public int period = 0;
    public String indicatorKey = null;


    public HashMap<ZonedDateTime, Bar> bars = new HashMap();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    public Mt4Source(String symbol, int period, String ohlcvCsvFile, String[] indicatorFiles) {
        this.symbol = symbol;
        this.period = period;
        List<String[]> ohlcv = loadCsvData(ohlcvCsvFile);
        HashMap<String, List<String[]>> indicators = new HashMap<>();
        for (int i = 0; i < indicatorFiles.length; i++) {
            indicators.put(indicatorFiles[i], loadCsvData(indicatorFiles[i]));
//            System.out.println(indicatorFiles[i]+" "+loadCsvData(indicatorFiles[i]).size());
        }
        merge(ohlcv,indicators);

    }

    public void merge(List<String[]> ohlcv, HashMap<String, List<String[]>> indicators) {

        Duration barDuration = Duration.ofSeconds(period * 60);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        ZonedDateTime zonedDateTime=null;
        Bar bar=null;
        String date="EMPTY";
        int o=0;
        try {
            for (String[] priceData : ohlcv) {
                zonedDateTime = dateFormatter.parse(priceData[0]).toInstant().atZone(ZoneId.systemDefault());
               bar = new BaseBar( zonedDateTime, priceData[1], priceData[2], priceData[3], priceData[4], priceData[5], DoubleNum::valueOf);
                bars.put(zonedDateTime,bar);
            }

            for (String indicatorName: indicators.keySet()){
                int bufferNumber=indicators.get(indicatorName).get(0).length-1;
                 o=0;
                List<String[]> cc=indicators.get(indicatorName);
                for (String[] indicatorBuffers :indicators.get(indicatorName)){
                    zonedDateTime = dateFormatter.parse(indicatorBuffers[0]).toInstant().atZone(ZoneId.systemDefault());
                    DoubleNum[] bufferValues=new DoubleNum[bufferNumber];
                    for (int i = 0; i < bufferNumber; i++)  bufferValues[i]=DoubleNum.valueOf(indicatorBuffers[i+1]);
                    if (bars.containsKey(zonedDateTime)) {
                        bar = bars.get(zonedDateTime);
                        bar.addBuffer(indicatorName, bufferValues);
                    }
                }
//                System.out.println(indicatorName+" "+o);
            }

        } catch (Exception e) {
            System.out.println("DATAE: "+date);
            e.printStackTrace();
        }

//        public BaseBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {
//        Bar bar = new BaseBar(barDuration, barEndTime, series.function());

    }

    public Mt4Source(String fileName, String symbol, int period) {

        this.symbol = symbol;
        this.period = period;
        loadCsvData(fileName);
//        int i=0;
//        Num[] barValue=new Num[5];
//        try {
//            do {
//                // get a trade
//                String[] line = lines.get(i);
//                ZonedDateTime zonedDateTime = dateFormatter.parse(line[0]).toInstant().atZone(ZoneId.systemDefault());
//                barValue[0]= DoubleNum.valueOf(line[1]);
//                barValue[1]= DoubleNum.valueOf(line[2]);
//                barValue[2]= DoubleNum.valueOf(line[3]);
//                barValue[3]= DoubleNum.valueOf(line[4]);
//                barValue[4]= DoubleNum.valueOf(line[5]);
//                value.put(zonedDateTime,barValue);
//                i++;
//            } while (i < lines.size());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    public Mt4Source(String fileName, String symbol, int period, String indicatorKey) {

        this.symbol = symbol;
        this.period = period;
        this.indicatorKey = indicatorKey;

        loadCsvData(fileName);
    }

    private List<String[]> loadCsvData(String fileName) {

        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(Mt4Source.class.getClassLoader().getResource(fileName).getPath()));
            int index=0;
            List<String[]> result=new ArrayList<>();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row =Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);
                result.add(row);
            }
            return result;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }
}
