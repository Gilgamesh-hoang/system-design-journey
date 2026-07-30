package com.example.idempotency;

import com.example.idempotency.ride.RideRequest;
import com.example.idempotency.ride.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis is started via the {@code docker} CLI directly (not Testcontainers' bundled
 * docker-java client, which fails to negotiate with this machine's Docker Desktop —
 * see build notes) and wired in with {@link DynamicPropertySource}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IdempotencyIT {

    private static final String REDIS_CONTAINER_ID = startRedisContainer();
    private static final int REDIS_PORT = hostPortOf(REDIS_CONTAINER_ID);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.master.host", () -> "localhost");
        registry.add("redis.master.port", () -> REDIS_PORT);
        registry.add("redis.password", () -> "");
    }

    private static String startRedisContainer() {
        try {
            Process run = new ProcessBuilder("docker", "run", "-d", "--rm", "-p", "127.0.0.1::6379", "redis:7-alpine").start();
            String containerId = new String(run.getInputStream().readAllBytes()).trim();
            if (run.waitFor() != 0) {
                throw new IllegalStateException("docker run failed: " + new String(run.getErrorStream().readAllBytes()));
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stopRedisContainer(containerId)));
            return containerId;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("could not start redis container for tests", e);
        }
    }

    private static int hostPortOf(String containerId) {
        try {
            Process inspect = new ProcessBuilder("docker", "inspect", "-f",
                    "{{(index (index .NetworkSettings.Ports \"6379/tcp\") 0).HostPort}}", containerId).start();
            String port = new String(inspect.getInputStream().readAllBytes()).trim();
            inspect.waitFor();
            return Integer.parseInt(port);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void stopRedisContainer(String containerId) {
        try {
            new ProcessBuilder("docker", "stop", containerId).start().waitFor();
        } catch (IOException | InterruptedException ignored) {
        }
    }

    @LocalServerPort
    int port;

    @Autowired
    RideService rideService;

    RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void sameKeyTenTimes_createsExactlyOneRide_andReturnsIdenticalResponse() {
        String key = UUID.randomUUID().toString();
        int before = rideService.findAll().size();

        EntityExchangeResult<String> first = post(key, new RideRequest("A", "B"));
        assertThat(first.getStatus()).isEqualTo(HttpStatus.CREATED);

        for (int i = 0; i < 9; i++) {
            EntityExchangeResult<String> repeat = post(key, new RideRequest("A", "B"));
            assertThat(repeat.getStatus()).isEqualTo(first.getStatus());
            assertThat(repeat.getResponseBody()).isEqualTo(first.getResponseBody());
        }

        assertThat(rideService.findAll().size() - before).isEqualTo(1);
    }

    @Test
    void concurrentRequestsSameKey_createOnlyOneRide() throws InterruptedException {
        String key = UUID.randomUUID().toString();
        int before = rideService.findAll().size();

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                await(go);
                post(key, new RideRequest("C", "D"));
            });
        }
        ready.await();
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(rideService.findAll().size() - before).isEqualTo(1);
    }

    @Test
    void sameKeyDifferentBody_isRejectedWithConflict() {
        String key = UUID.randomUUID().toString();
        post(key, new RideRequest("A", "B"));

        EntityExchangeResult<String> second = post(key, new RideRequest("X", "Y"));

        assertThat(second.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void missingIdempotencyKey_isRejectedWithBadRequest() {
        EntityExchangeResult<String> response = client.post().uri("/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RideRequest("A", "B"))
                .exchange()
                .returnResult(String.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private EntityExchangeResult<String> post(String key, RideRequest body) {
        return client.post().uri("/rides")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .returnResult(String.class);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
