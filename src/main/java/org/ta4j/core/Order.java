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
package org.ta4j.core;

import org.ta4j.core.cost.CostModel;
import org.ta4j.core.cost.ZeroCostModel;
import org.ta4j.core.extendedCore.BaseTradingRecordExtended;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.SLTPManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An order.
 * </p>
 * The order is defined by:
 * <ul>
 *     <li>the index (in the {@link TimeSeries time series}) it is executed
 *     <li>a {@link OrderType type} (BUY or SELL)
 *     <li>a pricePerAsset (optional)
 *     <li>an amount to be (or that was) ordered (optional)
 * </ul>
 * A {@link Trade trade} is a pair of complementary orders.
 */
public class Order implements Serializable {

	private static final long serialVersionUID = -905474949010114150L;

	/**
     * The type of an {@link Order order}.
     * <p>
     * A BUY corresponds to a <i>BID</i> order.<p>
     * A SELL corresponds to an <i>ASK</i> order.
     */
    public enum OrderType {

        BUY {
            @Override
            public OrderType complementType() {
                return SELL;
            }

            @Override
            public boolean isBuy() {
                return true;
            }

        },
        SELL {
            @Override
            public OrderType complementType() {
                return BUY;
            }

            @Override
            public boolean isBuy() {
                return false;
            }


        };

        /**
         * @return the complementary order type
         */
        public abstract OrderType complementType();
        public abstract boolean isBuy();

    }


    public static enum ExitType {
        EXITRULE,
        TAKEPROFIT,
        STOPLOSS,
        ENDSERIES
    }

    
    /** Type of the order */
    private OrderType type;

    /** The index the order was executed */
    private int index;

    /** The pricePerAsset for the order */
    private Num pricePerAsset;

    /** The net price for the order, net transaction costs */
    private Num netPrice= NaN.NaN;
    
    /** The amount to be (or that was) ordered */
    private Num amount;

    /** Cost of executing the order */
    private Num cost;

    /** Model for the costs */
    private CostModel costModel=new ZeroCostModel();

    /** Order ID */
    private int id;

    /** Takeprofit */
//    public TakeProfit takeProfit=new TakeProfit(this);
//
//    /** StopLoss */
//    public StopLoss stopLoss=new StopLoss(this);

    public Num takeProfit=null,takeProfitAmount;
    public Num stopLoss=null,stopLossAmount;


    /** ---  részben teljesüléshez */
    public Num fulfilled= DoubleNum.valueOf(0);

//    /** -- entry order ID-je */
//    public Integer entryOrderId =null;

    /** entry order esetén a kilépő pár, vagy párok listája */
    public List<Order> exitOrders= new ArrayList<>();

    /* a trading history-hoz és a TimeSeries-hez való hozzáféréshez */
    public BaseTradingRecordExtended tradingRecord;


    /** Type of the exit */
    public ExitType exitType;

    //** Closed flag for Entry orders
    private boolean closed=false;

    // lezárás index-e
    public int closeIndex=Integer.MAX_VALUE;
    public Num closePrice;
    public Num amountToClose=DoubleNum.valueOf(0.0);

    //
    public String comment="";

    public Num maxProfit=DoubleNum.valueOf(0.0);
    public Num maxLoss=DoubleNum.valueOf(Double.MAX_VALUE);

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param series the time series
     * @param type the type of the order
     */
    protected Order(int index, TimeSeries series, OrderType type) {
        this(index, series, type, series.numOf(1));
    }

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param series the time series
     * @param type the type of the order
     * @param amount the amount to be (or that was) ordered
     */
    protected Order(int index, TimeSeries series, OrderType type, Num amount) {
        this(index, series, type, amount, new ZeroCostModel());
    }

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param series the time series
     * @param type the type of the order
     * @param amount the amount to be (or that was) ordered
     */
    protected Order(int index, TimeSeries series, OrderType type, Num amount, CostModel transactionCostModel) {
        this.type = type;
        this.index = index;
        this.amount = amount;

        setPricesAndCost(series.getBar(index).getClosePrice(), amount, transactionCostModel);
    }

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     * @param pricePerAsset the pricePerAsset for the order
     */
    protected Order(int index, OrderType type, Num pricePerAsset) {
        this(index, type, pricePerAsset, pricePerAsset.numOf(1));
    }

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     * @param pricePerAsset the pricePerAsset for the order
     * @param amount the amount to be (or that was) ordered
     */
    protected Order(int index, OrderType type, Num pricePerAsset, Num amount) {
        this(index, type, pricePerAsset, amount, new ZeroCostModel());
    }

    protected Order(OrderType type,int index, Num price, Num amount,Integer orderId,BaseTradingRecordExtended tradingRecord) {

        this.tradingRecord=tradingRecord;
        this.index = index;
        this.amount = amount;
        this.pricePerAsset=price;
        this.id=orderId;
        this.type = type;

//        setStopLoss(index);
//        setTakeProfit(index);

//        if (type.isBuy()) {
//            for (SLTPManager takeProfitManager : tradingRecord.buyStrategy.takeProfitManagers) takeProfitManager.set(index,this);
//            for (SLTPManager stopLossManager : tradingRecord.buyStrategy.stopLossManagers) stopLossManager.set(index,this);
//        } else {
//            for (SLTPManager takeProfitManager : tradingRecord.sellStrategy.takeProfitManagers) takeProfitManager.set(index,this);
//            for (SLTPManager stopLossManager : tradingRecord.sellStrategy.stopLossManagers) stopLossManager.set(index,this);
//        }



//        BackTestContext backTestContext=BackTestContext.getInstance();
//        if (type==OrderType.BUY) {
//            if (backTestContext.buyStrategy.takeProfitManager!=null) takeProfitManager=backTestContext.buyStrategy.takeProfitManager;
//            if (backTestContext.buyStrategy.stopLossManager!=null) stopLossManager=backTestContext.buyStrategy.stopLossManager;
//
//        } else {
//            if (backTestContext.sellStrategy.takeProfitManager!=null) takeProfitManager=backTestContext.sellStrategy.takeProfitManager;
//            if (backTestContext.sellStrategy.stopLossManager!=null) stopLossManager=backTestContext.sellStrategy.stopLossManager;
//        }
//        if (takeProfitManager!=null) takeProfitManager.set(index,this);
//        if (stopLossManager!=null) stopLossManager.set(index,this);

        setPricesAndCost(price, amount, costModel);

    }

    protected Order(int index, Order entryOrder,Integer orderId,Order.ExitType exitType) {

        this.index = index;
        amount = entryOrder.amountToClose;
        if (exitType==Order.ExitType.STOPLOSS)
            this.pricePerAsset=entryOrder.stopLoss;
        if (exitType== ExitType.TAKEPROFIT)
            this.pricePerAsset=entryOrder.takeProfit;
        if (exitType== ExitType.EXITRULE)
            this.pricePerAsset=entryOrder.closePrice;
        id=orderId;
        type=entryOrder.getType().complementType();
        this.exitType=exitType;
        this.closed=true;
        this.comment=exitType.toString();

        if (entryOrder.fulfilled.plus(amount).isGreaterThanOrEqual(entryOrder.getAmount())) {
            entryOrder.fulfilled=entryOrder.getAmount();
            entryOrder.close(index);
        } else {
            entryOrder.fulfilled=entryOrder.fulfilled.plus(amount);
        }
        entryOrder.exitOrders.add(this);
        setPricesAndCost(this.pricePerAsset, amount, costModel);

    }

    public void setTakeProfit(int index){
//        Num takeProfitValue;
//        if (type.isBuy()) {
//            for (SLTPManager takeProfitManager : tradingRecord.buyStrategy.takeProfitManagers) {
//                takeProfitValue=takeProfitManager.getValue(index,this);
//                if (takeProfitValue!=null) {
////                    takeProfit.set(id, takeProfitValue, getOpenAmount(), takeProfitManager.getName());
//                    if (tradingRecord.debug)  tradingRecord.debug(index, id, takeProfitValue.getDelegate(), takeProfitManager.hashCode());
//                }
//            }
//
//        } else {
//            for (SLTPManager takeProfitManager : tradingRecord.sellStrategy.takeProfitManagers) {
//                takeProfitValue=takeProfitManager.getValue(index,this);
//                if (takeProfitValue!=null) {
////                    takeProfit.set(id, takeProfitValue, getOpenAmount(), takeProfitManager.getName());
//                    if (tradingRecord.debug)  tradingRecord.debug(index, id, takeProfitValue.getDelegate(), takeProfitManager.hashCode());
//                }
//            }
//
//        }


    }

    public void setStopLoss(int index){
//        Num stopLossValue;
//        if (type.isBuy()) {
//            for (SLTPManager stopLossManager : tradingRecord.buyStrategy.stopLossManagers) {
//                stopLossValue=stopLossManager.getValue(index,this);
//                if (stopLossValue!=null) {
//
////                    stopLoss.set(id, stopLossValue, getOpenAmount(), stopLossManager.getName());
//                    if (tradingRecord.debug) tradingRecord.debug(index, id, stopLossValue.getDelegate(), stopLossManager.hashCode());
//                }
//            }
//        } else {
//            for (SLTPManager stopLossManager : tradingRecord.sellStrategy.stopLossManagers) {
//                stopLossValue=stopLossManager.getValue(index,this);
//                if (stopLossValue!=null) {
////                    stopLoss.set(id, stopLossValue, getOpenAmount(), stopLossManager.getName());
//                    if (tradingRecord.debug) tradingRecord.debug(index, id, stopLossValue.getDelegate(), stopLossManager.hashCode());
//                }
//            }
//        }

    }





    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     * @param pricePerAsset the pricePerAsset for the order
     * @param amount the amount to be (or that was) ordered
     * @param transactionCostModel Cost model for order execution cost
     */
    protected Order(int index, OrderType type, Num pricePerAsset, Num amount, CostModel transactionCostModel) {
        this.type = type;
        this.index = index;
        this.amount = amount;

        setPricesAndCost(pricePerAsset, amount, transactionCostModel);
    }

    public Num getOpenAmount(){ return amount.minus(fulfilled);}

    /**
     * @return the type of the order (BUY or SELL)
     */
    public OrderType getType() {
        return type;
    }


    public Num getCost() { return cost; }

    /**
     * @return the index the order is executed
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the pricePerAsset for the order
     */
    public Num getPricePerAsset() { return pricePerAsset; }

    /**
     * @return the pricePerAsset for the order, net transaction costs
     */
    public Num getNetPrice() { return netPrice; }

    /**
     * @return the amount to be (or that was) ordered
     */
    public Num getAmount() {
        return amount;
    }

    public CostModel getCostModel() { return costModel; }


    /**
     * Sets the raw and net prices of the order
     * @param pricePerAsset raw price of the asset
     * @param amount amount of assets ordered
     * @param transactionCostModel model of transaction cost
     */
    private void setPricesAndCost(Num pricePerAsset, Num amount, CostModel transactionCostModel) {
        this.costModel = transactionCostModel;
        this.pricePerAsset = pricePerAsset;
        this.cost = transactionCostModel.calculate(this.pricePerAsset, amount);

        Num costPerAsset = cost.dividedBy(amount);
        // add transaction costs to the pricePerAsset at the order
        if (type.equals(OrderType.BUY)) {
            this.netPrice = this.pricePerAsset.plus(costPerAsset);
        }
        else {
            this.netPrice = this.pricePerAsset.minus(costPerAsset);
        }
    }

    /**
     * @return true if this is a BUY order, false otherwise
     */
    public boolean isBuy() {
        return type == OrderType.BUY;
    }

    /**
     * @return true if this is a SELL order, false otherwise
     */
    public boolean isSell() {
        return type == OrderType.SELL;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isOpen() {
        return !closed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, index, pricePerAsset, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        return (this.pricePerAsset == other.pricePerAsset || (this.pricePerAsset != null && this.pricePerAsset.equals(other.pricePerAsset)))
                && (this.amount == other.amount || (this.amount != null && this.amount.equals(other.amount)));
    }

    @Override
    public String toString() {
        StringBuffer header=new StringBuffer();
        StringBuffer detail=new StringBuffer();
        Double profit=0.0,totalProfit=0.0;

        header.append(id).append(". ").append(type).append(" order");
        detail.append("{entry=").append(index).append(", price=").
                append(pricePerAsset).append(", amount=").append(amount).append("/").append(fulfilled);
        if (exitOrders.size()==0) detail.append("}");
        for (Order exitOrder: exitOrders){
            detail.append("\r\n").append(exitOrder.exitType).append(" exit=").append(exitOrder.index).append(", price=").
            append(exitOrder.pricePerAsset).append(", amount=").append(exitOrder.amount);
            if (getType()==OrderType.BUY) profit=exitOrder.pricePerAsset.minus(pricePerAsset).multipliedBy(amount).doubleValue();
            else profit=pricePerAsset.minus(exitOrder.pricePerAsset).multipliedBy(amount).doubleValue();
            detail.append(", profit=").append(profit);
            totalProfit+=profit;

        }
        if (exitOrders.size()>0) detail.append("}");
        header.append(", Total profit=").append(totalProfit).append("\r\n");
        return header.append(detail).toString();
        //return "Order{" + "type=" + type + ", index=" + index + ", price=" + pricePerAsset + ", amount=" + amount + ", fulfilled=" + fulfilled+'}';
    }

//    public static Order buyAt(int index, Num price, Num amount, Integer orderId,BaseStrategyExtended buyStrategy) {
//        return new Order(index, series, OrderType.BUY);
//    }

    /**
     * @param index the index the order is executed
     * @param series the time series
     * @return a BUY order
     */
    public static Order buyAt(int index, TimeSeries series) {
        return new Order(index, series, OrderType.BUY);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) bought
     * @return a BUY order
     */
    public static Order buyAt(int index, Num price, Num amount, CostModel transactionCostModel) {
        return new Order(index, OrderType.BUY, price, amount, transactionCostModel);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) bought
     * @return a BUY order
     */
    public static Order buyAt(int index, Num price, Num amount) {
        return new Order(index, OrderType.BUY, price, amount);
    }


    public static Order openAt(OrderType type,int index, Num price, Num amount,Integer orderId,BaseTradingRecordExtended tradingRecord){
        return new Order(type, index, price, amount ,orderId,tradingRecord );
    }


    public static Order closeAt(int index, Order entryOrder,Integer orderId,Order.ExitType exitType){
        return new Order(index, entryOrder,orderId,exitType );
    }


    /**
     * @param index the index the order is executed
     * @param series the time series
     * @param amount the amount to be (or that was) bought
     * @return a BUY order
     */
    public static Order buyAt(int index, TimeSeries series, Num amount) {
        return new Order(index, series, OrderType.BUY, amount);
    }

    /**
     * @param index the index the order is executed
     * @param series the time series
     * @param amount the amount to be (or that was) bought
     * @return a BUY order
     */
    public static Order buyAt(int index, TimeSeries series, Num amount, CostModel transactionCostModel) {
        return new Order(index, series, OrderType.BUY, amount, transactionCostModel);
    }

    /**
     * @param index the index the order is executed
     * @param series the time series
     * @return a SELL order
     */
    public static Order sellAt(int index, TimeSeries series) {
        return new Order(index, series, OrderType.SELL);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) sold
     * @return a SELL order
     */
    public static Order sellAt(int index, Num price, Num amount) {
        return new Order(index, OrderType.SELL, price, amount);
    }


    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) sold
     * @return a SELL order
     */
    public static Order sellAt(int index, Num price, Num amount, CostModel transactionCostModel) {
        return new Order(index, OrderType.SELL, price, amount, transactionCostModel);
    }

    /**
     * @param index the index the order is executed
     * @param series the time series
     * @param amount the amount to be (or that was) bought
     * @return a SELL order
     */
    public static Order sellAt(int index, TimeSeries series, Num amount) {
        return new Order(index, series, OrderType.SELL, amount);
    }

    /**
     * @param index the index the order is executed
     * @param series the time series
     * @param amount the amount to be (or that was) bought
     * @return a SELL order
     */
    public static Order sellAt(int index, TimeSeries series, Num amount, CostModel transactionCostModel) {
        return new Order(index, series, OrderType.SELL, amount, transactionCostModel);
    }

    /**
     * @return the value of an order (without transaction cost)
     */
    public Num getValue() {
        return pricePerAsset.multipliedBy(amount);
    }

    public void setId(int id){
        this.id=id;
    }

    public int  getId(){ return id;}


    public void close(int index){
        closeIndex=index;
        fulfilled=getAmount();
        closed=true;
    }

    public boolean  getClosed(){ return closed;}




}
