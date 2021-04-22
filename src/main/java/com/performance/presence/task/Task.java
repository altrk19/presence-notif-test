package com.performance.presence.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.performance.presence.constant.HttpStatusCodeRange;
import com.performance.presence.model.NotificationMessage;
import com.performance.presence.util.HttpStatusCodeRangeUtil;
import com.performance.presence.util.NotificationMessageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class Task implements Callable<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(Task.class);
    public static AtomicInteger successCount = new AtomicInteger(0);
    public static AtomicInteger failCount = new AtomicInteger(0);

    private String destinationAddress;
    private int friendCount;
    private int sleepTime;
    private List<String> userChannelList;
    private int loop;
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Task(String destinationAddress, int friendCount, int sleepTime, List<String> userChannelList, int loop) {
        this.destinationAddress = destinationAddress;
        this.friendCount = friendCount;
        this.sleepTime = sleepTime;
        this.userChannelList = userChannelList;
        this.loop = loop;
    }

    @Override
    public Boolean call() throws InterruptedException {
        while (!userChannelList.isEmpty()) {
            for (int i = 0; i < friendCount; i++) {
                NotificationMessage notificationMessage =
                        NotificationMessageGenerator.generateNotificationMessage(userChannelList.get(0), "b" + (i + 1));
                ResponseEntity<String> response;
                try {
                    String requestUrl =
                            destinationAddress + "/cpaas/callback/v1/" + userChannelList.get(0) + "/external/" +
                                    userChannelList.get(1) + "/spidr";
//                    log.debug("Sending request :{} ", requestUrl);
                    response = sendRequests(requestUrl, notificationMessage);

//                    log.debug("RESPONSE subscriber : {} status code : {}", userChannelList.get(0), response.getStatusCode());


//                    log.debug("Response for subscriber : {} ws channel : {} contact : {} response : {}",
//                            userChannelList.get(0), userChannelList.get(1),
//                            notificationMessage.getPresenceWatcherNotificationParams().getName(),
//                            response.getStatusCode());
                    if (HttpStatusCodeRangeUtil.getRange(response.getStatusCodeValue()) ==
                            HttpStatusCodeRange.SUCCESS_RANGE) {
                        successCount.getAndIncrement();
                    } else {
                        failCount.getAndIncrement();
                    }
//                    log.debug("Success count : {}, Fail Count : {} for Loop : {}", successCount, failCount, loop);
                    Thread.sleep(sleepTime);
                } catch (Exception ex) {

//                    log.error(ex.getMessage());
                    failCount.getAndIncrement();
                    Thread.sleep(sleepTime);
//                    log.debug("Success count : {}, Fail Count : {} for Loop : {}", successCount, failCount, loop);
                }
            }
            userChannelList.remove(0);
            userChannelList.remove(0);
        }
        return true;
    }

    private ResponseEntity<String> sendRequests(String requestUrl, NotificationMessage notificationMessage)
            throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = objectMapper.writeValueAsString(notificationMessage);
//        log.debug("Sending rest Request to : {} with the following body : {}", destinationAddress, body);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(requestUrl, request, String.class);
    }

}
