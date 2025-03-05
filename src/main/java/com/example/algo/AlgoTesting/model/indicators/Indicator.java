package com.example.algo.AlgoTesting.model.indicators;

import java.util.List;

public interface Indicator {

    double get();

    void update(double newPrice);

    void init(List<Double> closingPrices);
}
