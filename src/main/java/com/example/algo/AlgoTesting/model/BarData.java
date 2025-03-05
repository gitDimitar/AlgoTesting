package com.example.algo.AlgoTesting.model;

import com.example.algo.AlgoTesting.model.indicators.Indicator;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class BarData {
    private double open;
    private double close;
    private double high;
    private double low;
    private int tradeCount;
    private int volume ;
    private double vwap;
    private Date dateTime;
    private boolean green = true;
    Map<String, Indicator> indicatorList;

    public BarData(double open, double close, double high, double low, int tradeCount, int volume, double vwap, Date dateTime) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.tradeCount = tradeCount;
        this.volume = volume;
        this.vwap = vwap;
        this.dateTime = dateTime;
        indicatorList = new HashMap<>();
        this.green = calculateColour(open, close);
    }


    public void addIndicator(String indicatorName, Indicator indicator) {
        indicatorList.put(indicatorName, indicator);
    }

    public Indicator getIndicator(String indicatorName) {
        return indicatorList.get(indicatorName);
    }

    public boolean isGreen() {
        return green;
    }

    public boolean calculateColour(double open, double close) {
        if(open < close) {
            return true;
        } else {
            return false;
        }
    }
}
