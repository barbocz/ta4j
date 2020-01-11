package org.test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.h2.jdbcx.JdbcConnectionPool;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.strategy.myEntryStrategies.KeltnerEntry;
import org.strategy.myExitStrategies.KeltnerExit;
import org.ta4j.core.Bar;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import javafx.scene.control.cell.PropertyValueFactory;

public class TradeCenter {

    int portNumber = 5000;
    int barIndex = 0;

    MT4TimeSeries sampleSeries;
    public HashMap<String, TimeSeriesRepo> timeSeriesRepos = new HashMap<>();
    public HashMap<String, TradeEngine> tradeEngines = new HashMap<>();

    @FXML
    private TableView priceDataTableView;

    @FXML
    private TableView tradingCoreDataTableView;

    @FXML
    public Label info;

    @FXML
    private TextField symbol;

    @FXML
    private TextField entryStrategyName;

    @FXML
    private TextField exitStrategyName;

    @FXML
    private TextField period;

    @FXML
    private RadioButton minuteRadioButton;

    @FXML
    private RadioButton seriesRadioButton;

    @FXML
    private RadioButton tickRadioButton;

    ObservableList<PriceData> timeSeriesObservableList = FXCollections.observableArrayList();
    ObservableList<TradingCoreData> tradingStrategiesObservableList = FXCollections.observableArrayList();

    public void initialize() {


        TableColumn symbolCol = new TableColumn("Symbol");
        symbolCol.setMinWidth(20);
        symbolCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("symbol"));
        priceDataTableView.getColumns().add(symbolCol);

        TableColumn periodCol = new TableColumn("TimeFrames");
        periodCol.setMinWidth(60);
        periodCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("timeFrame"));
        priceDataTableView.getColumns().add(periodCol);

        TableColumn portCol = new TableColumn("Port");
        portCol.setMinWidth(20);
        portCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("port"));
        priceDataTableView.getColumns().add(portCol);

        TableColumn barNumber = new TableColumn("Bars");
        barNumber.setMinWidth(30);
        barNumber.setCellValueFactory(new PropertyValueFactory<PriceData, String>("barNumber"));
        priceDataTableView.getColumns().add(barNumber);

        TableColumn beginTime = new TableColumn("From");
        beginTime.setMinWidth(100);
        beginTime.setCellValueFactory(new PropertyValueFactory<PriceData, String>("beginTime"));
        priceDataTableView.getColumns().add(beginTime);

        TableColumn endTime = new TableColumn("To");
        endTime.setMinWidth(100);
        endTime.setCellValueFactory(new PropertyValueFactory<PriceData, String>("endTime"));
        priceDataTableView.getColumns().add(endTime);


        TableColumn ohlcvCol = new TableColumn("OHLCV");
        ohlcvCol.setMinWidth(300);
        ohlcvCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("ohlcv"));
        priceDataTableView.getColumns().add(ohlcvCol);



        TableColumn idCol = new TableColumn("ID");
        idCol.setMinWidth(20);
        idCol.setCellValueFactory(new PropertyValueFactory<PriceData, Integer>("id"));
        tradingCoreDataTableView.getColumns().add(idCol);

        TableColumn tradingSymbolCol = new TableColumn("Symbol");
        tradingSymbolCol.setMinWidth(100);
        tradingSymbolCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("symbol"));
        tradingCoreDataTableView.getColumns().add(tradingSymbolCol);

        TableColumn entryStrategyCol = new TableColumn("Entry Strategy");
        entryStrategyCol.setMinWidth(200);
        entryStrategyCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("entryStrategy"));
        tradingCoreDataTableView.getColumns().add(entryStrategyCol);

        TableColumn exitStrategyCol = new TableColumn("Exit Strategy");
        exitStrategyCol.setMinWidth(200);
        exitStrategyCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("exitStrategy"));
        tradingCoreDataTableView.getColumns().add(exitStrategyCol);

        TableColumn infoCol = new TableColumn("Info");
        infoCol.setMinWidth(400);
        infoCol.setCellValueFactory(new PropertyValueFactory<PriceData, String>("info"));
        tradingCoreDataTableView.getColumns().add(infoCol);


        sampleSeries = new MT4TimeSeries.SeriesBuilder().
                withName("*").
                withPeriod(1).
                withOhlcvFileName("EURUSD_1_TEST.csv").
//                withDateFormatPattern("yyyy.MM.dd HH:mm").
        withDateFormatPattern("dd.MM.yyyy HH:mm:ss.SSS").
                        withNumTypeOf(DoubleNum.class).
                        build();


        addSymbol(new ActionEvent());

        addStrategy(new ActionEvent());

    }

    public TradeCenter() {
        TradeCenter controller = this;
        Runnable myRunnable =
                new Runnable() {
                    public void run() {
                        try (ZContext context = new ZContext()) {
                            ZMQ.Socket serverSocket = context.createSocket(SocketType.REP);
                            serverSocket.bind("tcp://:5000");

                            while (!Thread.currentThread().isInterrupted()) {
                                byte[] reply = serverSocket.recv(0);
                                final String messageSymbol = new String(reply, ZMQ.CHARSET);

                                String response = "Trading core is already exists";


//                                String items[] = message.split(";");  // Tcp channel kiosztás üzenetformátuma symbol;timeFrame -> EURUSD;3
//                                String messageSymbol = items[0];
//                                int messagePeriod = Integer.parseInt(items[1]);
                                boolean exists = false;
                                for (String symbol : timeSeriesRepos.keySet()) {
                                    if (symbol.equals(messageSymbol)) {
                                        exists = true;
                                        response = Integer.toString(timeSeriesRepos.get(symbol).portNumber);

//                                        timeSeriesRepos.get(symbol).coreSeries=null;
//                                        timeSeriesRepos.get(symbol).timeSeries=new TreeMap<>();
//                                        timeSeriesRepos.get(symbol).barIndex=new HashMap<>();
//                                        timeSeriesRepos.get(symbol).setTimeSeries(1);

                                        System.out.println(symbol + " price feeding already exists on port: " + response);
                                        break;
                                    }
                                }
                                if (!exists) {
                                    portNumber=calculatePortNumber(messageSymbol);
                                    System.out.println("Init price feeding thread for: " + messageSymbol + " on port: " + portNumber);
                                    response = Integer.toString(portNumber);
                                    TimeSeriesRepo timeSeriesRepo = new TimeSeriesRepo(messageSymbol, portNumber, controller);
                                    timeSeriesRepo.setTimeSeries(1);
                                    timeSeriesRepos.put(messageSymbol, timeSeriesRepo);


                                }
                                serverSocket.send(response.getBytes(ZMQ.CHARSET), 0);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateTimeSeriesRepoHeader(messageSymbol + ";" + Integer.toString(portNumber));
                                        info.setText(messageSymbol);
                                    }
                                });

                            }
                        }
                    }
                };


        Thread thread = new Thread(myRunnable);
        thread.start();


    }

    public void updateBalance(int strategyID, double balance){
        int index = 0;
        for (TradingCoreData strategy : tradingStrategiesObservableList) {
            if (strategy.getId()== strategyID) {
                strategy.setInfo(String.valueOf(balance));
                tradingStrategiesObservableList.set(index, strategy);
                tradingCoreDataTableView.setItems(tradingStrategiesObservableList);

                break;
            }
            index++;
        }

//        tradingStrategiesObservableList.add(new TradingCoreData(tradeEngine.logStrategy.id,key, entryStrategyName.getText(), exitStrategyName.getText(), "info"));
//        tradingCoreDataTableView.setItems(tradingStrategiesObservableList);
    }

    public void updateTimeSeriesRepo(int portNumber, String message) {
//        System.out.println(portNumber+" port, message:  "+message);
        int index = 0;

        DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        for (PriceData tradingCore : timeSeriesObservableList) {
            if (tradingCore.getPort() == portNumber) {

                TimeSeriesRepo tsRepo = timeSeriesRepos.get(tradingCore.getSymbol());
//                System.out.println("ENDINDEX "+tsRepo.getTimeSeries(1).getEndIndex());
                int endIndex = tsRepo.getTimeSeries(1).getEndIndex();
                if (endIndex>-1) {
                    tradingCore.setBarNumber(endIndex + 1);
                    tradingCore.setEndTime(zdtFormatter.format(tsRepo.getTimeSeries(1).getBar(endIndex).getEndTime()));
                    tradingCore.setBeginTime(zdtFormatter.format(tsRepo.getTimeSeries(1).getBar(0).getEndTime()));
                }
                if (message.contains("(")) tradingCore.setTimeFrame(message);
                else tradingCore.setOhlcv(message);
                timeSeriesObservableList.set(index, tradingCore);
                priceDataTableView.setItems(timeSeriesObservableList);
                break;
            }
            index++;
        }
    }

    void updateTimeSeriesRepoHeader(String message) {
//        System.out.println(message);
        // message: symbol;port  EURUSD;5001
        timeSeriesObservableList = priceDataTableView.getItems();
        String messageParts[] = message.split(";");
        boolean found = false;
        for (PriceData tradingCore : timeSeriesObservableList) {
            if (tradingCore.getSymbol().equals(messageParts[0])) {
                found = true;
                break;
            }
        }
        if (!found) {
//            System.out.println("updateTradingCoreTable: "+messageParts[0]);
            timeSeriesObservableList.add(new PriceData(messageParts[0], "", Integer.parseInt(messageParts[1]), 0, "", "", ""));
            priceDataTableView.setItems(timeSeriesObservableList);
        }
    }

    int calculatePortNumber(String symbol){
        int portNumber=0;
        for (int j = 0;  j < symbol.length(); j++) {
            if(j%2==0) portNumber+=(int)symbol.charAt(j) * Math.pow(2.0,j);
            if(j%2==1) portNumber+=(int)symbol.charAt(j) * Math.pow(3.0,j);
        }
        return portNumber;
    }

    public int getPort(String symbol) {

        // timeSeriesRepo-ból visszaadja az adott instrumentumhoz tartozó port számot
        for (String repoSymbol : timeSeriesRepos.keySet()) {
            if (repoSymbol.equals(symbol)) {
                return timeSeriesRepos.get(symbol).portNumber;
            }
        }

//        System.out.printf(symbol.getText()+";"+timeFrame.getText());

        // ha nincs még a timeSeriesRepo-ban ilyen instrumentum, az 5000-es porton figyelő szál (ami a TradeCenter constructor-ban jött létre) kioszt neki egyet és létrehozza az instrumentumot a timeSeriesRepo-ban
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:5000");

        socket.send(symbol.getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv(0);
        try {
            return Integer.parseInt(new String(reply, ZMQ.CHARSET));

        } catch (NumberFormatException ex) {
            portNumber = 0;
        }
//        System.out.println("Received " + portNumber);

        socket.close();
        context.close();

        return 0;


    }

    public void sendPrice(ActionEvent e) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);

        socket.connect("tcp://localhost:" + getPort(symbol.getText()));

//        System.out.println(tradingCores.get(symbol.getText()+";"+timeFrame.getText()));

        // Barchange , az MT4 most küldené az új bar adatait
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        // T - első tag = TICK
        // M - első tag = perces adat ohlcv formátumban
        String request = "";

        if (minuteRadioButton.isSelected()) {
            double avgTickPrice=(sampleSeries.getBar(barIndex).getOpenPrice().doubleValue() + sampleSeries.getBar(barIndex).getClosePrice().doubleValue())/2.0;
            request = "T;" +  avgTickPrice + ";" + avgTickPrice;
            socket.send(request.getBytes(ZMQ.CHARSET), 0);
            socket.recv(0);

            request = "M;" + dateFormatter.format(Date.from(sampleSeries.getBar(barIndex).getEndTime().toInstant())) + ";" + sampleSeries.getBar(barIndex).getOpenPrice() + ";" + sampleSeries.getBar(barIndex).getHighPrice() + ";" + sampleSeries.getBar(barIndex).getLowPrice() + ";" + sampleSeries.getBar(barIndex).getClosePrice() + ";" + sampleSeries.getBar(barIndex).getVolume();
            socket.send(request.getBytes(ZMQ.CHARSET), 0);
            socket.recv(0);

            barIndex++;
        } else if (tickRadioButton.isSelected())
            request = "T;" + sampleSeries.getBar(barIndex).getHighPrice() + ";" + sampleSeries.getBar(barIndex).getLowPrice();
        else if (seriesRadioButton.isSelected()) {
            long startTime = System.currentTimeMillis();

            for (TradeEngine tradeEngine : tradeEngines.values()) {
                if (tradeEngine.islogged) tradeEngine.logStrategy.setAutoCommit(false);
            }
            while (barIndex < sampleSeries.getEndIndex() - 1) {
                request = "M;" + dateFormatter.format(Date.from(sampleSeries.getBar(barIndex).getEndTime().toInstant())) + ";" + sampleSeries.getBar(barIndex).getOpenPrice() + ";" + sampleSeries.getBar(barIndex).getHighPrice() + ";" + sampleSeries.getBar(barIndex).getLowPrice() + ";" + sampleSeries.getBar(barIndex).getClosePrice() + ";" + sampleSeries.getBar(barIndex).getVolume();
                socket.send(request.toString().getBytes(ZMQ.CHARSET), 0);
                socket.recv(0);
//                try {
//                    Thread.sleep(20);
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
                barIndex++;

            }
            System.out.println("Series feed has ended in " + (System.currentTimeMillis() - startTime) + " ms ");
            for (TradeEngine tradeEngine : tradeEngines.values()) {
                if (tradeEngine.islogged) tradeEngine.logStrategy.setAutoCommit(true);
            }
            // TODO logolást rendbe tenni

//            System.out.println("");
//            timeSeriesRepos.get("SYMBOL").toString(1440);
//            System.out.println("");
//            timeSeriesRepos.get("SYMBOL").toString(60);
//            for (int i = 0; i < 10; i++) {
//                Bar bar= timeSeriesRepos.get("SYMBOL").getTimeSeries(3).getBar(i);
//                System.out.println(i+". "+bar.getEndTime()+":  "+bar.getOpenPrice()+", "+bar.getHighPrice()+", "+bar.getLowPrice()+", "+bar.getClosePrice()+", "+bar.getVolume());
//            }

        }

        socket.close();
        context.close();

    }

    public void addSymbol(ActionEvent e) {
        getPort(symbol.getText());

    }

    public void startEngine(String symbol,int timeFrame){

        if (!timeSeriesRepos.containsKey(symbol)) {
            System.out.println("WARNING::: timeSeriesRepo for "+symbol+" is missing!!!");
            return;
        }
        String key=symbol+","+timeFrame+","+entryStrategyName.getText()+","+exitStrategyName.getText();
        if (!tradeEngines.containsKey(key)) {

//            Class cls = Class.forName("weshEntity." + entityType);
//            WeshSuperEntity wse = (WeshSuperEntity) cls.newInstance();

//            tradingStrategiesObservableList.add(new TradingCoreData(key, entryStrategyName.getText(), exitStrategyName.getText(), "info"));
//            tradingCoreDataTableView.setItems(tradingStrategiesObservableList);
            TradeCenter controller = this;

            Runnable myRunnable =
                    new Runnable() {
                        public void run() {
                            try {

                                TimeSeriesRepo timeSeriesRepo=timeSeriesRepos.get(symbol);
                                Class<?> clazz = Class.forName("org.strategy.myEntryStrategies." + entryStrategyName.getText());
                                Constructor<?> constructor = clazz.getConstructor();
                                Strategy entryStrategy = (Strategy) constructor.newInstance();
                                clazz = Class.forName("org.strategy.myExitStrategies." + exitStrategyName.getText());
                                constructor = clazz.getConstructor();
                                Strategy exitStrategy = (Strategy) constructor.newInstance();
////                                TradeEngine tradeEngine = new TradeEngine(key, entryStrategy, exitStrategy, controller);
                                timeSeriesRepo.processType= TimeSeriesRepo.ProcessType.MT4;
                                TradeEngine tradeEngine=new TradeEngine(timeSeriesRepo,timeFrame,entryStrategy,exitStrategy,controller,TradeEngine.LogLevel.TOTAL);
                                tradeEngine.initStrategy();
//
////                                TradeEngine tradeEn = (TradeEngine)constructor.newInstance(key,Integer.parseInt(period.getText()), timeSeriesRepos.get(symbol.getText()),controller);
////                                tradeEn.initStrategy(true);
//
                                tradeEngines.put(key, tradeEngine);
                                tradingStrategiesObservableList.add(new TradingCoreData(tradeEngine.logStrategy.id,key, entryStrategyName.getText(), exitStrategyName.getText(), "info"));
                                tradingCoreDataTableView.setItems(tradingStrategiesObservableList);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };


            Thread thread = new Thread(myRunnable);
            thread.start();

        }

    }


    public void addStrategy(ActionEvent e) {
        startEngine(symbol.getText(),Integer.parseInt(period.getText()));
    }

    public void checkOnMt4(ActionEvent e) {
        TradingCoreData selectedLine=(TradingCoreData)tradingCoreDataTableView.getSelectionModel().getSelectedItem();
        if (selectedLine!=null) {
            TradeEngine tradeEngine=tradeEngines.get(selectedLine.getSymbol());
            if (tradeEngine!=null) {
                try {
                    tradeEngine.logStrategy.getMT4data(tradeEngine.logStrategy.id);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    public void test(ActionEvent e) {
        getPort("GBPUSD");
        startEngine("GBPUSD", 3);







//        try {
//            JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:~\\ta4j;ACCESS_MODE_DATA=r","sa","12345");
//            Connection conn = cp.getConnection();
//
////        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD","EURUSD_3MONTH.csv","yyyy.MM.dd HH:mm");
////
////        Strategy testEntry=new TestEntry(3,timeSeriesRepo);
////        System.out.println(getSourceContent(testEntry));
//
////        Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/C:\\\\Users\\\\Barbocz Attila\\\\ta4j;AUTO_SERVER=TRUE;user=sa;password=12345");
////        Connection conn = DriverManager.getConnection("jdbc:h2:~\\ta4j","sa","12345");
//            PreparedStatement prs = conn.prepareStatement("select * from TEST");
//            ResultSet rs=prs.executeQuery();
//            while (rs.next()) {
//                System.out.println(rs.getString(2));
//            }
//
//            conn.close();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }


//        for (TradeEngine tradeEn: tradeEngines.values()){
//            System.out.println(tradeEn.getClass()+"::::");
//            for (String sName: tradeEn.timeSeriesRepos.strategies.keySet())
//            System.out.println(sName);
//        }

//        for(Integer tf: timeSeriesRepos.get("SYMBOL").timeSeries.keySet()){
//            int endIndex= timeSeriesRepos.get("SYMBOL").timeSeries.get(tf).getEndIndex();
//            if (endIndex<0) continue;
//            System.out.println(tf+": "+ timeSeriesRepos.get("SYMBOL").timeSeries.get(tf).getBar(endIndex-1).getEndTime());
//
//            for (int i = 0; i < 100; i++) {
//                Bar bar= timeSeriesRepos.get("SYMBOL").timeSeries.get(tf).getBar(i);
//                System.out.println(i+". "+bar.getEndTime()+":  "+bar.getOpenPrice()+", "+bar.getHighPrice()+", "+bar.getLowPrice()+", "+bar.getClosePrice()+", "+bar.getVolume());
//            }
//            System.out.println(" ");
//        }

//        TimeSeriesRepo timeSeriesRepo = new TimeSeriesRepo("EURUSD_1_TEST.csv");
////        Strategy keltnerEntry = new KeltnerEntry(3, timeSeriesRepo);
////        Strategy keltnerExit = new KeltnerExit(3, timeSeriesRepo);
////        TradeEngine tradeEngine = new TradeEngine("test", keltnerEntry, keltnerExit, this);
////        tradeEngine.initStrategy();
////        tradeEngine.runBackTest();
//        TimeSeriesRepo timeSeriesRepo = timeSeriesRepos.get("EURUSD");
//        for(MT4TimeSeries timeSeries: timeSeriesRepo.timeSeries.values()) System.out.println("Period"+timeSeries.period + " " + timeSeries.getEndIndex());
//            for (TradeEngine tradeEngine : tradeEngines.values()) {
//
//                System.out.println("bid: " + timeSeriesRepo.bid + " - ask:" + timeSeriesRepo.ask);
//                System.out.println("prevIndex: " + tradeEngine.series.getPrevIndex() + " - currentIndex:" + tradeEngine.series.getCurrentIndex());
//                System.out.println("currentBar otime " + tradeEngine.series.getCurrentBar().getBeginTime() +
//                        ", etime:" + tradeEngine.series.getCurrentBar().getEndTime() +
//                        ", oprice:" + tradeEngine.series.getCurrentBar().getOpenPrice() +
//                        ", cprice:" + tradeEngine.series.getCurrentBar().getClosePrice()+
//                        ", vol:" + tradeEngine.series.getCurrentBar().getVolume());
//            }

    }

    public void click(ActionEvent e) {

        timeSeriesRepos.get(symbol.getText()).setTimeSeries(Integer.parseInt(period.getText()));

        if (timeSeriesRepos.get(symbol.getText()).getTimeSeries(3).getEndIndex() < 0) return;
        Bar bar = timeSeriesRepos.get("SYMBOL").getTimeSeries(3).getBar(0);
        System.out.println(0 + ". " + bar.getEndTime() + ":  " + bar.getOpenPrice() + ", " + bar.getHighPrice() + ", " + bar.getLowPrice() + ", " + bar.getClosePrice() + ", " + bar.getVolume());


//        ZContext context = new ZContext();
//        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
//
//        socket.connect("tcp://localhost:" + getPort(symbol.getText()));
//
//        socket.addSymbol(period.getText().getBytes(ZMQ.CHARSET), 0);
//        socket.recv(0);
//
//        socket.close();
//        context.close();


//        String message="2019.02.06 11:00;1.13914;1.13926;1.13912;1.1393;98.0";
//        int portNumber=5001;
//        System.out.println(portNumber+" port, message:  "+message);
//        int index=0;
//        for (PriceData tradingCore: timeSeriesObservableList){
//            if (tradingCore.getPort()==portNumber) {
//                tradingCore.setOhlcv(message);
//                timeSeriesObservableList.set(index,tradingCore);
//                tradingCoresTableView.setItems(timeSeriesObservableList);
//                System.out.println("FOUNDDDDD");
//                break;
//            }
//            index++;
//        }

//        for (TradingCore trading : tradingCores) {
//
//            for (Integer timeFrame : trading.strategy.timeSeries.keySet()) {
//                System.out.println(trading.id + " "+timeFrame);
//                MT4TimeSeries series = trading.strategy.timeSeries.get(timeFrame);
//                for (int i = 0; i < series.getEndIndex(); i++) {
//
//                    System.out.println(series.getBar(i).getEndTime() + ";" + series.getBar(i).getOpenPrice() + ";" + series.getBar(i).getHighPrice() + ";" + series.getBar(i).getLowPrice() + ";" + series.getBar(i).getClosePrice() + ";" + series.getBar(i).getVolume());
//                }
//
//
//            }
//
//        }

    }
}