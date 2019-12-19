package org.test;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public  class PriceData {
    public final SimpleStringProperty symbol;
    public final SimpleStringProperty timeFrame;
    public final SimpleIntegerProperty port;
    public final SimpleIntegerProperty barNumber;
    public final SimpleStringProperty beginTime;
    public final SimpleStringProperty endTime;
    public final SimpleStringProperty ohlcv;

    public PriceData(String symbol, String timeFrame, Integer port,Integer barNumber,String beginTime,String endTime, String ohlcv) {
        this.symbol = new SimpleStringProperty(symbol);
        this.timeFrame = new SimpleStringProperty(timeFrame);
        this.port = new SimpleIntegerProperty(port);
        this.barNumber=new SimpleIntegerProperty(barNumber);
        this.beginTime=new SimpleStringProperty(beginTime);
        this.endTime=new SimpleStringProperty(endTime);
        this.ohlcv = new SimpleStringProperty(ohlcv);
    }

    public String getSymbol() {
        return symbol.get();
    }
    public void setSymbol(String fName) {
        symbol.set(fName);
    }

    public String getTimeFrame() {
        return timeFrame.get();
    }
    public void setTimeFrame(String fName) {
        timeFrame.set(fName);
    }

    public Integer getPort() {
        return port.get();
    }
    public void setPort(Integer fName) {
        port.set(fName);
    }

    public String getOhlcv() {
        return ohlcv.get();
    }
    public void setOhlcv(String fName) {
        ohlcv.set(fName);
    }


    public SimpleIntegerProperty barNumberProperty() {
        return barNumber;
    }

    public void setBarNumber(int barNumber) {
        this.barNumber.set(barNumber);
    }

    public String getBeginTime() {
        return beginTime.get();
    }

    public SimpleStringProperty beginTimeProperty() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime.set(beginTime);
    }

    public String getEndTime() {
        return endTime.get();
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }
}
