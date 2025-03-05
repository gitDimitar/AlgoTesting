package com.example.algo.AlgoTesting.model;

import lombok.Data;

import java.util.Date;

@Data
public class Trade {
    private String ticker;
    private final Date entryTime;
    private final double entryPrice; //Starting price of a trade (when logic decides to buy)
    private int position; //How much are you buying or selling. I.E 6 bitcoins or smth.
    private double averagePrice;
    private double commission = 1;
    private double closePrice = 0.0;
    private Date closeTime;
    private double stopLossPrice;
    private double targetProfitPrice;
    private double unrealizedGains;
    private double realizedGains;
    private double positionEquity;

    public Trade(String ticker, Date entryTime, double entryPrice, int position, double stopLossPrice, double targetProfitPrice, double commission) {
        this.commission = commission;
        this.targetProfitPrice = targetProfitPrice;
        this.ticker = ticker;
        this.entryTime = entryTime;
        this.entryPrice = entryPrice;
        this.position = position;
        this.averagePrice = ((position * entryPrice) + commission) / position;
        this.stopLossPrice = stopLossPrice;
        calculatePositionEquity();
    }

    private void calculatePositionEquity() {
        this.positionEquity = this.position * this.entryPrice;
    }

}
