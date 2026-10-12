package com.sumit.flashsale.flash_sale_storefront.service;

import com.sumit.flashsale.flash_sale_storefront.dto.LoadTestResultDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DashboardLoadTestService {

    private final MeterRegistry meterRegistry;
    private final String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public DashboardLoadTestService(
            MeterRegistry meterRegistry,
            @Value("${server.port:8080}") int port) {
        this.meterRegistry = meterRegistry;
        this.baseUrl = "http://localhost:" + port;
    }

    public LoadTestResultDto runReadTest(int durationSeconds) {
        if (durationSeconds < 1 || durationSeconds > 120) {
            throw new IllegalArgumentException(
                    "Duration must be between 1 and 120 seconds");
        }

        double readsBefore = readCounter();
        long started = System.nanoTime();
        long durationNanos = TimeUnit.SECONDS.toNanos(durationSeconds);
        long deadline = started + durationNanos;

        Map<Integer, AtomicLong> statusCounts = new ConcurrentHashMap<>();
        AtomicLong transportErrors = new AtomicLong();

        AtomicLong total = new AtomicLong();
        AtomicLong successful = new AtomicLong();
        AtomicLong failed = new AtomicLong();
        List<Long> latencies =
                Collections.synchronizedList(new ArrayList<>());

        var scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            scheduler.scheduleAtFixedRate(() -> {
                if (System.nanoTime() >= deadline) {
                    return;
                }

                int id = chooseProductId();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/api/products/" + id))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                long requestStarted = System.nanoTime();
                total.incrementAndGet();

                httpClient.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.discarding()
                ).whenComplete((response, error) -> {
                    latencies.add(TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - requestStarted));

                    if (error == null
                            && response.statusCode() >= 200
                            && response.statusCode() < 300) {
                        successful.incrementAndGet();
                    } else {
                        failed.incrementAndGet();
                    }
                });
            }, 0, 20, TimeUnit.MILLISECONDS);

            long remaining = deadline - System.nanoTime();
            if (remaining > 0) {
                TimeUnit.NANOSECONDS.sleep(remaining);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Load test was interrupted", ex);
        } finally {
            scheduler.shutdownNow();
        }

        // Wait for requests already launched to finish, up to 15 seconds.
        long waitUntil = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(15);

        while (successful.get() + failed.get() < total.get()
                && System.nanoTime() < waitUntil) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        long elapsedNanos = System.nanoTime() - started;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        double readsAfter = readCounter();

        List<Long> sortedLatencies;
        synchronized (latencies) {
            sortedLatencies = new ArrayList<>(latencies);
        }
        Collections.sort(sortedLatencies);

        double p95 = sortedLatencies.isEmpty() ? 0 :
                sortedLatencies.get((int) Math.ceil(
                        sortedLatencies.size() * 0.95) - 1);

        double readsPerSecond = Math.max(0,
                (readsAfter - readsBefore) / elapsedSeconds);

        boolean readRateWithinLimit = readsPerSecond <= 25.0;

        return new LoadTestResultDto(
                failed.get() == 0 ? "COMPLETED" : "COMPLETED_WITH_ERRORS",
                "Read workload completed. Inventory freshness and overselling "
                        + "require separate tests.",
                durationSeconds,
                total.get() / elapsedSeconds,
                total.get(),
                successful.get(),
                failed.get(),
                p95,
                readsPerSecond,
                readRateWithinLimit,
                null,
                null
        );
    }

    private int chooseProductId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 80% popular products; 20% from the remaining catalog.
        if (random.nextInt(100) < 80) {
            return random.nextInt(1, 1001);
        }
        return random.nextInt(1001, 50001);
    }

    private double readCounter() {
        var counter = meterRegistry
                .find("storefront.database.product.reads")
                .counter();

        return counter == null ? 0 : counter.count();
    }
}
