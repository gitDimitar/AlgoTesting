package com.example.algo.AlgoTesting.service;

import com.example.algo.AlgoTesting.model.Account;
import com.example.algo.AlgoTesting.model.BarData;
import com.example.algo.AlgoTesting.model.Trade;
import com.example.algo.AlgoTesting.model.indicators.EMA;
import com.example.algo.AlgoTesting.model.indicators.MACD;
import com.example.algo.AlgoTesting.model.indicators.RVOL;
import com.example.algo.AlgoTesting.model.indicators.SMAVolume;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AlgoService {

    @Autowired
    private FileUtils fileUtils;
    private List<BarData> barDataList;
    private List<Double> closingPrices;
    private List<Double> macdPrices;
    private List<Double> barVolumes;
    private Account account = new Account("Pullback trading $2000 starting capital", 2000);

    public void initiateTestByTicker(String ticker, String timeFrame) throws Exception {

        List<File> fileList = fileUtils.getFileList("src/main/resources/"+ timeFrame + "/", ticker);
        for(File file : fileList) {
            if(ticker == null) {
                ticker = file.getName().substring(0, file.getName().lastIndexOf('-'));
            }
            parseIntraDayFile(file, ticker);
        }
        //fileList.forEach(f -> System.out.println(f.getName()));
    }

    public void parseIntraDayFile(File file, String ticker) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNodeList;
        barDataList = new ArrayList<>();
        closingPrices = new ArrayList<>();
        barVolumes = new ArrayList<>();
        macdPrices = new ArrayList<>();

        jsonNodeList = objectMapper.readTree(file);
        jsonNodeList.forEach(n -> {
            try {
                barDataList.add(createBarData(n));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        //barDataList.forEach(b -> System.out.println(b.toString()));

        pullbackTradingStrategy(ticker);
    }


    public BarData createBarData(JsonNode jsonNode) throws ParseException {
        double open = jsonNode.get("o").asDouble();
        double close = jsonNode.get("c").asDouble();
        double high = jsonNode.get("h").asDouble();
        double low = jsonNode.get("l").asDouble();
        int tradeCount = jsonNode.get("n").asInt();
        int volume = jsonNode.get("v").asInt();
        double vwap = jsonNode.get("vw").asDouble();
        Date dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(jsonNode.get("t").asText());
        closingPrices.add(close);
        barVolumes.add((double)volume);

        BarData barData = new BarData(open, close, high, low, tradeCount, volume, vwap, dateTime);

        EMA ema12 = new EMA(closingPrices, 12);
        EMA ema26 = new EMA(closingPrices, 26);
        EMA ema10 = new EMA(closingPrices, 10);
        EMA ema20 = new EMA(closingPrices, 20);
        SMAVolume smaVol50 = new SMAVolume(barVolumes, 50);
        RVOL relativeVolume = new RVOL(volume, smaVol50.get());

        MACD macd = new MACD(ema12, ema26, closingPrices);
        macdPrices.add(macd.get());
        EMA signalLine = new EMA(macdPrices, 9);
        macd.update(signalLine.get());

        barData.addIndicator("12 EMA", ema12);
        barData.addIndicator("26 EMA", ema26);
        barData.addIndicator("10 EMA", ema10);
        barData.addIndicator("20 EMA", ema20);
        barData.addIndicator("50 SMA Vol", smaVol50);
        barData.addIndicator("RVOL", relativeVolume);
        barData.addIndicator("MACD", macd);

        return barData;
    }

    //Only trades if a red candle is immediately followed by a green candle that breaks the red candle's high
    public void pullbackTradingStrategy(String ticker) {
        //boolean prevCandleRed = false;
        BarData prevBar = new BarData();
        String recordTicker = ticker;
        for(BarData bar: barDataList) {
           if(bar.getIndicator("RVOL").get() >= 200
           && !prevBar.isGreen()
           && (account.getTrade(recordTicker) == null)
           && bar.getHigh() > prevBar.getHigh()) {
               recordTicker = ticker + "-" + bar.getDateTime();
               double entryPrice = prevBar.getHigh() +0.01;
               double stopLossPrice = prevBar.getLow() - 0.01;
               account.enterPosition(recordTicker, entryPrice,1.0, stopLossPrice, bar.getDateTime(), 3);
               prevBar = bar;
               continue;
           }

           if(account.getTrade(recordTicker) != null && account.getTrade(recordTicker).getClosePrice() == 0.0) {
               Trade curTrade = account.getTrade(recordTicker);
               if(curTrade.getStopLossPrice() >= bar.getLow()) {
                   account.exitPosition(recordTicker, curTrade.getStopLossPrice(), bar.getDateTime());
                   recordTicker = ticker;
               } else if (curTrade.getTargetProfitPrice() <= bar.getHigh()) {
                   account.exitPosition(recordTicker, curTrade.getTargetProfitPrice(), bar.getDateTime());
                   recordTicker = ticker;
               }

           }

            prevBar = bar;
           //prevCandleRed = !bar.isGreen();

        }
        if(account.getTrade(recordTicker) != null && account.getTrade(recordTicker).getClosePrice() == 0.0) {
            Trade curTrade = account.getTrade(recordTicker);
            account.exitPosition(recordTicker, prevBar.getClose(), prevBar.getDateTime());
        }

        account.getTrades().forEach((k,v) -> {System.out.println(k + " -> " + v + "\n");});
        account.printStatistics();

        System.out.println("Account Name: " + account.getName());
        System.out.println("Account Balance : " + account.getBalance());
        System.out.println("Account Net Liquidity : " + account.getNetLiquidity());
    }

}
