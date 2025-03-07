package com.example.algo.AlgoTesting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileUtils {

    public List<File> getFileList(String path, String ticker) {

        List<File> fileList = new ArrayList<>();
        File dir = new File(path);
        if(!dir.isDirectory()) throw new IllegalStateException("wtf mate?");
        for(File file : dir.listFiles()) {
            if(ticker != null && file.getName().startsWith(ticker)) {
                fileList.add(file);
            } else if (ticker == null) {
                fileList.add(file);
            }
        }
        return  fileList;
    }

}
