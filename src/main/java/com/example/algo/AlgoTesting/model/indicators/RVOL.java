package com.example.algo.AlgoTesting.model.indicators;

import lombok.Data;

import java.util.List;

@Data
public class RVOL implements Indicator {

    private double relativeVol = 0;

    public RVOL(double curVol, double SMAVol50) {
        relativeVol = (curVol / SMAVol50) * 100;
    }

    @Override
    public double get() {
        return relativeVol;
    }

    @Override
    public void update(double newPrice) {

    }

    @Override
    public void init(List<Double> closingPrices) {

    }
}
