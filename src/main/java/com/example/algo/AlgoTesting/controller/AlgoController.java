package com.example.algo.AlgoTesting.controller;

import com.example.algo.AlgoTesting.service.AlgoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgoController {

    @Autowired
    private AlgoService algoService;

    @GetMapping("/run")
    public void runAlgo() throws Exception {

        algoService.initiateTestByTicker("TRNR");

        //algoService.parseFile();
        //algoService.pullbackTradingStrategy();
    }
}
