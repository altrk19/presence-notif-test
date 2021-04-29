package com.performance.presence.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    private List<String> userChannelList = new ArrayList<>();
//    private SortedMap<String, String> userChannelMap = new TreeMap<>();

    public List<String> getUserChannelList(String fileName) {
        try(BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] splitStr = line.split("/");
                userChannelList.add(splitStr[5]);
                userChannelList.add(splitStr[7]);
            }
        } catch (IOException ex){
            log.error(fileName + " not found. Please make sure file exists");
            return Collections.emptyList();
        }
        return userChannelList;
    }

//    public SortedMap<String, String> getUserAndChannelMap() throws IOException {
//        try(BufferedReader in = new BufferedReader(new FileReader("resource.txt"))) {
//            String line;
//            while ((line = in.readLine()) != null) {
//                String[] splitStr = line.split("/");
//                userChannelMap.put(splitStr[5],splitStr[7]);
//            }
//        }
//        return userChannelMap;
//    }
}
