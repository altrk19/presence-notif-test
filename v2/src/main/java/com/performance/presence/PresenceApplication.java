package com.performance.presence;

import com.performance.presence.task.Task;
import com.performance.presence.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SpringBootApplication
public class PresenceApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PresenceApplication.class);

    private final FileUtil fileUtil;

    public PresenceApplication(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public static void main(String[] args) {
        SpringApplication.run(PresenceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
//        log.debug("Enter the thread count (example:4)");
//        int threadSize = scanner.nextInt();
        int rate = Integer.parseInt(args[0]);
        int threadSize;
        int counterLimit;
        if (rate == 640) {
            threadSize = 16;
            counterLimit = 40;
        } else if (rate == 320) {
            threadSize = 16;
            counterLimit = 20;
        } else {
            log.error("Not supported rate. Please choose one of [640,320]");
            System.exit(1);
            return;
        }

//        log.debug("Enter the number of contacts (example:20)");
//        int presenceNumber = scanner.nextInt();
        int presenceNumber = Integer.parseInt(args[1]);

//        log.debug("Enter the fileName with extension (example:resource.txt)");
//        String fileName = scanner.next();
//        String fileName = "resource.txt";
        String fileName = args[2];

//        log.debug("Enter the destination address (example:http://10.109.25.140:8080)");
//        String destinationAddress = scanner.next();
//        String destinationAddress = "http://10.109.25.140:8080";
        String destinationAddress = args[3];

//        log.debug("Enter the loop count (example:3)");
//        int loopCount = scanner.nextInt();
//        int loopCount = 120;
        int loopCount = Integer.parseInt(args[4]);
//

        log.debug("Starting read file");
        List<String> userChannelList = fileUtil.getUserChannelList(fileName);
        if (CollectionUtils.isEmpty(userChannelList)) {
            System.exit(1);
            return;
        }
        log.debug("Finished read file");


        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);

        for (int loop = 1; loop <= loopCount; loop++) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < threadSize; i++) {

                int skip = userChannelList.size() / threadSize;
                if (skip % 2 == 1) {
                    skip++;
                }

                List<String> taskList =
                        userChannelList.stream().skip(i * skip).limit(skip).collect(Collectors.toList());
                Task task = new Task(destinationAddress, presenceNumber, taskList, loop, counterLimit);
                Future<Boolean> result = executorService.submit(task);

                results.add(result);
            }
            int i = results.size() - 1;
            while (i > 0) {
                results.get(i).get();
                i--;
            }
            writeFile(loop, Task.successCount, Task.failCount, Task.totalSentCount);
        }


        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

//        log.debug("Finished all notifications");
//        log.debug("Total SUCCESS : {}", Task.successCount);
//        log.debug("Total FAIL : {}", Task.failCount);

        System.exit(1);
    }

    private void writeFile(int loop, AtomicInteger successCount, AtomicInteger failCount, AtomicInteger totalCount)
            throws IOException {
        FileWriter fileWriter = new FileWriter("testResults.txt", true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
//        printWriter.println("Total SUCCESS : " + successCount + " Total FAIL : " + failCount + " loop : " + loop);
        printWriter.println("loop : " + loop + " total count : " + totalCount);
        printWriter.close();
    }


}
