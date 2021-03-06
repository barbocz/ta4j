package org.strategy;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Order implements Cloneable {

    public enum Type {
        BUY {
            @Override
            public Type complementType() {
                return SELL;
            }

            @Override
            public boolean isBuy() {
                return true;
            }

            @Override
            public int mt4OrderType() {
                return 0;
            }

            @Override
            public boolean isHold() {
                return false;
            }

            @Override
            public String toString() {
                return "BUY";
            }
        },
        SELL {
            @Override
            public Type complementType() {
                return BUY;
            }

            @Override
            public boolean isBuy() {
                return false;
            }

            @Override
            public int mt4OrderType() {
                return 1;
            }

            @Override
            public boolean isHold() {
                return false;
            }

            @Override
            public String toString() {
                return "SELL";
            }
        },
        HOLD {

            @Override
            public Type complementType() {
                return HOLD;
            }

            @Override
            public boolean isBuy() {
                return false;
            }

            @Override
            public int mt4OrderType() {
                return -1;
            }

            @Override
            public boolean isHold() {
                return true;
            }

            @Override
            public String toString() {
                return "HOLD";
            }
        };

        public abstract Type complementType();
        public abstract boolean isBuy();
        public abstract int mt4OrderType();
        public abstract boolean isHold();
        public abstract String toString();
    }

    public enum Status {
        OPENED {
            @Override
            public boolean isOpened() {
                return true;
            }
            public boolean isClosed() {return false; }
        },
        CLOSED {
            @Override
            public boolean isOpened() {return false; }
            public boolean isClosed() {return true;  }
        };

        public abstract boolean isOpened();
        public abstract boolean isClosed();
    }


    public enum ExitType {
        EXITRULE {
            @Override
            public String toString() {
                return "EXIT";
            }
        },
        TAKEPROFIT{
            @Override
            public String toString() {
                return "TAKEPROFIT";
            }
        },
        STOPLOSS{
            @Override
            public String toString() {
                return "STOPLOSS";
            }
        },
        ENDSERIES{
            @Override
            public String toString() {
                return "ENDSERIES";
            }
        };
        public abstract String toString();
    }

    public Type type;
    public Status status;
    public ExitType exitType;

    public int id,parentId,barIndex, phase =0;
    public double openedAmount =0.0,openPrice=0.0,closePrice=0.0,cost=0.0,profit=0.0,takeProfit=0.0,stopLoss=0.0,closedAmount=0.0,comission=0.0;
    public double maxProfit=0.0,maxLoss=0.0;
    public List<Order> childOrders=new ArrayList<>();

    public double takeProfitTarget=0.0,stopLossTarget=0.0;

    public ZonedDateTime openTime,closeTime;

    // MT4-es paraméterek
    public int mt4TicketNumber=0,mt4NewTicketNumber=0;
    public long mt4MagicNumber=0;
    public ZonedDateTime mt4OpenTime,mt4NewOpenTime,mt4CloseTime;
    public double mt4OpenPrice=0.0,mt4NewOpenPrice=0.0,mt4ClosePrice=0.0,mt4Profit=0.0,mt4Lot=0.0;
    public String mt4Comment="";

    public boolean forcedClose=false;   // ha true akkor nem hívódik meg az Exit stratégia onExitEvent-je, hanem simán bezárásra kerül
    public boolean keepItsProfit=false; // ha true, akkor a TradeEngine nem enged olyan takeProfit-ot vagy stopLoss-t ami rosszabb eredményhez vezethet az előzőleg beállítottnál

    public HashMap<String,Double> doubleParameters=new HashMap<>();
    public HashMap<String,LinkedList<Double>> hashMapParameters=new HashMap<>();


    public Object clone()throws CloneNotSupportedException{
        return (Order)super.clone();
    }

    protected Order(Type type,double amount,double openPrice,ZonedDateTime openTime) {
        this.type=type;
        this.openedAmount =amount;
        this.openPrice=openPrice;
        this.openTime=openTime;

    }

    public static Order buy(double amount,double openPrice,ZonedDateTime openTime) {
        return new Order(Type.BUY, amount, openPrice, openTime);
    }

    public static Order sell(double amount,double openPrice,ZonedDateTime openTime) {
        return new Order(Type.SELL, amount, openPrice, openTime);
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getCurrentProfit(double currentPrice) {

        if (currentPrice==0.0) return 0.0;
        if (type == Order.Type.BUY) profit = (currentPrice - openPrice) * openedAmount;
        else profit = (openPrice - currentPrice) * openedAmount;

        return profit;
    }

    public double getClosedProfit() {
        if (type == Order.Type.BUY) profit = (closePrice - openPrice) * closedAmount;
        else profit = (openPrice - closePrice) * closedAmount;
        return profit;
    }
}
