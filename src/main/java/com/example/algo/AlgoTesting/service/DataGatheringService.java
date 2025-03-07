package com.example.algo.AlgoTesting.service;

import com.example.algo.AlgoTesting.model.BarData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataGatheringService {

    List<String> tickers = new ArrayList<>();
    List<String> pulledFileNames = new ArrayList<>();
    @Autowired
    FileUtils fileUtils;
    private static final int MIN_VOLUME = 10000000;
    private static final double MAX_PRICE = 20.0;
    private static final String PRE_MARKET_OPEN_TIME = "T09:00:00Z";
    private static final String MARKET_CLOSE_TIME = "T21:00:00Z";



    public void runDataGathering() throws IOException {
        //parseTickersFromCsv();
        //fetchBars(tickers, "1D", "2020-01-01", null);
        findGappingDays();


    }

    private void fetchBars(List<String> symbols, String timeframe, String startDate, String endDate) throws IOException {
        RestClient restClient = RestClient.builder().baseUrl("https://data.alpaca.markets/").build();
        String nextPageToken = null;
        String symbolsString = String.join(",", symbols);
        if(checkIfFileExists(symbols, timeframe, startDate)) return;

        do  {
            String uri = UriComponentsBuilder.fromPath("v2/stocks/bars")
                    .queryParam("symbols", symbolsString)
                    .queryParam("timeframe", timeframe)
                    .queryParam("start", startDate)
                    .queryParamIfPresent("end", Optional.ofNullable(endDate))
                    .queryParam("limit", "1000")
                    .queryParam("adjustment", "raw")
                    .queryParam("feed", "sip")
                    .queryParamIfPresent("page_token", Optional.ofNullable(nextPageToken))
                    .queryParam("sort", "asc")
                    .toUriString();
            uri = uri.replace("%3D", "=");

            String alpacaDailyDataResponse = restClient.get()
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("APCA-API-KEY-ID", "PKSVFXJMFE41EJJS5W9X")
                    .header("APCA-API-SECRET-KEY", "9PvHcdWv5eW2vHf7ekQxExrkKXIOYmNApk5A6Hoq")
                    .retrieve()
                    .body(String.class);

            nextPageToken = parseBarsResponse(alpacaDailyDataResponse, timeframe, startDate);
        } while (nextPageToken != null && !nextPageToken.equals("null"));
        removeDoubleSquareBrackets();
    }

    private boolean checkIfFileExists(List<String> symbols, String timeframe, String startDate) {
        if(symbols.size() > 1) return false;

        String fileName = "src/main/resources/" + timeframe +  "/" + symbols.get(0) + "-"+ startDate.substring(0, startDate.indexOf("T")) + "-" + timeframe + ".json";
        File file = new File(fileName);
        return file.exists();
    }

    private String parseBarsResponse(String jsonResponse, String timeframe, String date) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNodeList= objectMapper.readTree(jsonResponse);
        List<String> tickerList = new ArrayList<>();
        Iterator<String> iterator = jsonNodeList.get("bars").fieldNames();
        iterator.forEachRemaining(k -> tickerList.add(k));

        tickerList.forEach(k -> {
                try {
                    String fileName;
                    if(timeframe != "1D") {
                        fileName = "src/main/resources/" + timeframe +  "/" + k + "-"+ date.substring(0, date.indexOf("T")) + "-" + timeframe + ".json";
                    } else {
                        fileName = "src/main/resources/" + timeframe +  "/" + k + "-" + timeframe + ".json";
                    }
                    File file = new File(fileName);
                    if(!file.exists()){
                        file.createNewFile();
                        pulledFileNames.add(file.getPath());
                        System.out.println("New File Created: " + file.getName());
                    }

                    //true = append file
                    FileWriter fileWritter = new FileWriter(file,true);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write(String.valueOf(jsonNodeList.get("bars").get(k)));
                    bufferWritter.close();
                    fileWritter.close();

                    System.out.println("Done");
                }catch(Exception e){
                    e.printStackTrace();
                }
        });
        return jsonNodeList.get("next_page_token").asText();
    }

    private void removeDoubleSquareBrackets() throws IOException {
        pulledFileNames.forEach(fn -> {
            try {
                Path path = Paths.get(fn);
                Charset charset = StandardCharsets.UTF_8;

                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll("]\\[", ",");
                Files.write(path, content.getBytes(charset));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    //TODO: if data exists for that day, skip it, don't override file
    private void findGappingDays() {
        List<File> dailyList = fileUtils.getFileList("src/main/resources/1D/", null);

        dailyList.forEach(f -> {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNodeList;
            double prevClose = 0.0;
            try {
                jsonNodeList = objectMapper.readTree(f);
                String ticker = f.getName().substring(0, f.getName().lastIndexOf('-'));
                for(JsonNode n :jsonNodeList) {
                    double close = n.get("c").asDouble();
                    if (prevClose == 0.0) {
                        prevClose = close;
                        continue;
                    }

                    double open = n.get("o").asDouble();
                    int volume = n.get("v").asInt();
                    if (prevClose < MAX_PRICE
                            && open <= MAX_PRICE
                            && open >= (prevClose *= 1.3)
                            && volume >= MIN_VOLUME) {
                        String day = (n.get("t").asText().substring(0, n.get("t").asText().indexOf("T")));
                        String startDate = day + PRE_MARKET_OPEN_TIME;
                        String endDate = day + MARKET_CLOSE_TIME;
                        fetchBars(List.of(ticker), "1min", startDate, endDate);
                        fetchBars(List.of(ticker), "5min", startDate, endDate);
                    }
                    prevClose = close;
                }
            } catch (IOException e){
                    throw new RuntimeException(e);
            }
        });

    }

    private void parseTickersFromCsv() {
        tickers.add("TRNR");
        tickers.add("BTAI");
        tickers.add("FMTO");
        tickers.add("SOPA");
        tickers.add("GTBP");
        tickers.add("WAFU");
        /*
        try (CSVReader csvReader = new CSVReader(new FileReader("src/main/resources/screener/small_caps_1_to_20_2025-03-05.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                if(csvReader.getLinesRead() == 1) continue;
                tickers.add(values[0]);
            }
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
         */
    }
}
