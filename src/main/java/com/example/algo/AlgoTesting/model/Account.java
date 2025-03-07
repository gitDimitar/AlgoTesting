package com.example.algo.AlgoTesting.model;

import lombok.Data;

import java.util.*;

@Data
public class Account {

    private String name;
    private double netLiquidity;
    private double balance;
    private TreeMap<String, Trade> trades;
    private int winningTrades = 0;
    private int losingTrades = 0;
    private double averageWinner = 0.0;
    private double averageLoser = 0.0;
    private double winPercentage = 0.0;




    public Account(String name, double netLiquidity) {
        this.name = name;
        this.netLiquidity = netLiquidity;
        this.balance = netLiquidity;
        trades = new TreeMap<>();
    }

    //Assume one entry and one exit per position
    public void enterPosition(String ticker, double entryPrice, double riskPercent, double stopLossPrice,  Date entryTime, double targetProfitRatio ) {
        int position = calculatePositionSize(entryPrice, stopLossPrice, riskPercent);
        double requiredBalance = entryPrice * position;
        if(balance < requiredBalance) {
            System.out.println("Balance: " + balance + " is less than required to enter position in "+ ticker + " , required balance: " + requiredBalance + " !");
            return;
        }

        addTrade(buy(ticker, entryTime, entryPrice, position, stopLossPrice, targetProfitRatio));
        balance -= requiredBalance;
    }

    public Trade buy(String ticker, Date entryTime, double entryPrice, int position, double stopLossPrice, double targetProfitRatio) {
        double targetProfitPrice = entryPrice + ((entryPrice - stopLossPrice) * targetProfitRatio);
        return new Trade(ticker, entryTime, entryPrice, position, stopLossPrice,targetProfitPrice, 1);
    }

    public void exitPosition(String ticker, double closePrice, Date closeTime) {
        Trade curTrade = this.trades.get(ticker);
        curTrade.setClosePrice(closePrice);
        curTrade.setCloseTime(closeTime);
        double tradePL = ((closePrice - curTrade.getAveragePrice()) * curTrade.getPosition()) - curTrade.getCommission();
        balance += (tradePL + curTrade.getPositionEquity());
        netLiquidity += tradePL;
        curTrade.setRealizedGains(tradePL);
        updateStatistics(tradePL);
    }

    public void addTrade(Trade trade) {
        this.trades.put(trade.getTicker(), trade);
    }

    public Trade getTrade(String ticker) {
        return trades.get(ticker);
    }

    private int calculatePositionSize(double entryPrice, double stopLossPrice, double riskPercent) {
        double riskDollarAmount = (netLiquidity * (riskPercent/100));
        int position = (int) Math.round(riskDollarAmount / (entryPrice - stopLossPrice));

        return position;

    }

    private void updateStatistics(double tradePL) {
        if(Double.compare(tradePL, 0) < 0) {
            losingTrades++;
        } else {
            winningTrades++;
        }
        winPercentage = ((double) winningTrades / (winningTrades + losingTrades)) * 100;

    }

    public void printStatistics() {
        System.out.println("Total trades : " + (winningTrades + losingTrades));
        System.out.println("Winning trades : " + winningTrades);
        System.out.println("Loss % : " + (100 - winPercentage));
        System.out.println("Win % : " + winPercentage);
    }


}
