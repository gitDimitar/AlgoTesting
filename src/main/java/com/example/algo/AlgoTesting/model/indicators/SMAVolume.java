package com.example.algo.AlgoTesting.model.indicators;

import lombok.Data;

import java.util.List;

@Data
public class SMAVolume implements Indicator {

    private double currentSMAVol;
    private int period;
    private double multiplier;
    private String explanation;

    public SMAVolume(List<Double> barVolumes, int period) {
        currentSMAVol = 0;
        this.period = period;
        this.explanation = period + " Day SMA Volume";
        init(barVolumes);
    }

    @Override
    public double get() {
        return currentSMAVol;
    }

    @Override
    public void update(double newVolume) {
    }

    @Override
    public void init(List<Double> volumeList) {
        // SMAVol calculation
        for (int i = 0; i < period; i++) {
            if(volumeList.size() < period && i == volumeList.size()) {
                break;
            }
            currentSMAVol += volumeList.get(volumeList.size() - (i+1));
        }

        currentSMAVol = currentSMAVol / period;
    }
}
