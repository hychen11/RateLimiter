package com.hychen11.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class RateLimiter {
    private final int MAX_REQUESTS_PER_SECOND = 10;
    private final Map<Long, AtomicInteger> window1 = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> window2 = new ConcurrentHashMap<>();

    @GetMapping("/test1")
    public String tryAcquire1() {
        long currentSecond = System.currentTimeMillis() / 1000;
        window1.putIfAbsent(currentSecond, new AtomicInteger(0));
        int count = window1.get(currentSecond).incrementAndGet();
        if (count > MAX_REQUESTS_PER_SECOND) {
            throw new RuntimeException("exceed max request per second 1");
        }
        window1.keySet().removeIf(second -> second < currentSecond - 2);
        return "tryAcquire1 passed";
    }

    @GetMapping("/test2")
    public String tryAcquire2(@RequestParam String request) {
        long currentSecond = System.currentTimeMillis() / 1000;
        window2.putIfAbsent(currentSecond, new ArrayList<>());
        List<String> requests = window2.get(currentSecond);
        synchronized (requests){
            if(requests.size()>=MAX_REQUESTS_PER_SECOND){
                throw new RuntimeException("exceed max request per second 2");
            }
            requests.add(request);
        }
        window2.keySet().removeIf(second -> second < currentSecond - 2);
        return "tryAcquire2 passed";
    }
}

