package com.example.algo.AlgoTesting.controller;

import com.example.algo.AlgoTesting.service.AlgoService;
import com.example.algo.AlgoTesting.service.DataGatheringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgoController {

    @Autowired
    private AlgoService algoService;
    @Autowired
    private DataGatheringService dataGatheringService;

    @GetMapping("/run")
    public void runAlgo() throws Exception {

        algoService.initiateTestByTicker("WAFU", "1min");

        //algoService.parseFile();
        //algoService.pullbackTradingStrategy();
    }

    @GetMapping("/runAll")
    public void runAlgoAll() throws Exception {

        algoService.initiateTestByTicker(null, "1min");

        //algoService.parseFile();
        //algoService.pullbackTradingStrategy();
    }

    @GetMapping("/data")
    public void runDataGathering() throws Exception {

        dataGatheringService.runDataGathering();

    }

}
