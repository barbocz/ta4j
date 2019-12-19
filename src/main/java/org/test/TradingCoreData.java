package org.test;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TradingCoreData {

    public final SimpleIntegerProperty id;
    public final SimpleStringProperty symbol;
    public final SimpleStringProperty entryStrategy;
    public final SimpleStringProperty exitStrategy;
    public final SimpleStringProperty info;

    public TradingCoreData(Integer id,String symbol, String entryStrategy, String exitStrategy, String info) {
        this.id=new SimpleIntegerProperty(id);
        this.symbol = new SimpleStringProperty(symbol);
        this.entryStrategy = new SimpleStringProperty(entryStrategy);
        this.exitStrategy = new SimpleStringProperty(exitStrategy);
        this.info = new SimpleStringProperty(info);
    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public String getSymbol() {
        return symbol.get();
    }

    public void setSymbol(String fName) {
        symbol.set(fName);
    }

    public String getEntryStrategy() {
        return entryStrategy.get();
    }

    public void setEntryStrategy(String fName) {
        entryStrategy.set(fName);
    }

    public String getExitStrategy() {
        return exitStrategy.get();
    }

    public void setExitStrategy(String fName) {
        exitStrategy.set(fName);
    }


    public String getInfo() {
        return info.get();
    }

    public void setInfo(String fName) {
        info.set(fName);
    }


}
