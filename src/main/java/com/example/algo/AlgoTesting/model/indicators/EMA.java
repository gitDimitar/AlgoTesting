package com.example.algo.AlgoTesting.model.indicators;

import lombok.Data;

import java.util.List;

@Data
public class EMA implements Indicator {
    private double currentEMA;
    private int period;
    private double multiplier;
    private String explanation;

    public EMA(List<Double> closingPrices, int period) {
        currentEMA = 0;
        this.period = period;
        this.multiplier = 2.0 / (double) (period + 1);
        this.explanation = period + " Day EMA";
        init(closingPrices);
    }

    @Override
    public double get() {
        return currentEMA;
    }

    @Override
    public void update(double newPrice) {
        currentEMA = (newPrice - currentEMA) * multiplier + currentEMA;
    }

    @Override
    public void init(List<Double> closingPrices) {
        if (period > closingPrices.size()) return;

        //Initial SMA
        for (int i = 0; i < period; i++) {
            currentEMA += closingPrices.get(i);
        }
        //TODO: Rework - remove the forloop and just pass last(current) EMA as value
        currentEMA = currentEMA / (double) period;
        for (int i = period; i < closingPrices.size() - 1; i++) {
            update(closingPrices.get(i));
        }
    }
}
