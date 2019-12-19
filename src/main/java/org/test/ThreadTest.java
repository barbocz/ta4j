package org.test;

public class ThreadTest {

    public String symbol;
    public ThreadTest(String symbol) {
        this.symbol=symbol;

    }

    public void run(){
        System.out.println(symbol);
    }
}
