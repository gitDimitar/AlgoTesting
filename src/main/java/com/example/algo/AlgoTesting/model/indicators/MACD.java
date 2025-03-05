package com.example.algo.AlgoTesting.model.indicators;

import lombok.Data;

import java.util.List;

@Data
public class MACD implements Indicator {

    private double currentMACD;
    private double currentSignal;
    private EMA shortEMA; //Will be the EMA object for shortEMA-
    private EMA longEMA; //Will be the EMA object for longEMA.
    private final int period; //Only value that has to be calculated in setInitial.
    private final double multiplier;
    private final int periodDifference;
    private String explanation;
    public static double SIGNAL_CHANGE;

    public MACD(EMA shortEMA, EMA longEMA, List<Double> closingPrices, int shortPeriod, int longPeriod, int signalPeriod) {
        //this.shortEMA = new EMA(closingPrices, shortPeriod); //true, because history is needed in MACD calculations.
        //this.longEMA = new EMA(closingPrices, longPeriod); //true for the same reasons.
        this.shortEMA = shortEMA;
        this.longEMA = longEMA;
        this.period = signalPeriod;
        this.multiplier = 2.0 / (double) (signalPeriod + 1);
        this.periodDifference = longPeriod - shortPeriod;
        explanation = "";
        init(closingPrices); //initializing the calculations to get current MACD and signal line.
    }

    @Override
    public double get() {
        return currentMACD - currentSignal;
    }

    @Override
    public void update(double newPrice) {
        //Updating the EMA values before updating MACD and Signal line.
        //lastTick = get();
        //shortEMA.update(newPrice);
        //longEMA.update(newPrice);
        currentMACD = shortEMA.get() - longEMA.get();
        currentSignal = currentMACD * multiplier + currentSignal * (1 - multiplier);
    }

    @Override
    public void init(List<Double> closingPrices) {

    }


}
