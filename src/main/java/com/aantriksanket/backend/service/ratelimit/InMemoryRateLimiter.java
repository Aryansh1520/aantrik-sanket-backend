package com.aantriksanket.backend.service.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter {

    private static class Counter {
        int count;
        long windowStart;
    }

    private final Map<String, Counter> store = new ConcurrentHashMap<>();

    public boolean allow(String key, int limit, int windowSeconds) {
        long now = Instant.now().getEpochSecond();

        Counter counter = store.computeIfAbsent(key, k -> {
            Counter c = new Counter();
            c.count = 0;
            c.windowStart = now;
            return c;
        });

        synchronized (counter) {
            if (now - counter.windowStart >= windowSeconds) {
                counter.count = 0;
                counter.windowStart = now;
            }

            counter.count++;
            return counter.count <= limit;
        }
    }
}
