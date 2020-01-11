package org.strategy;

import java.time.ZonedDateTime;

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
        };

        public abstract Type complementType();
        public abstract boolean isBuy();
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
        EXITRULE,
        TAKEPROFIT,
        STOPLOSS,
        ENDSERIES
    }

    public Type type;
    public Status status;
    public ExitType exitType;

    public int id,parentId,barIndex,closePhase=0;
    public double openedAmount =0.0,openPrice=0.0,closePrice=0.0,cost=0.0,profit=0.0,takeProfit=0.0,stopLoss=0.0,closedAmount=0.0;
    public double maxProfit=0.0,maxLoss=0.0;

    public ZonedDateTime openTime,closeTime;

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
