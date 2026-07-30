package com.example.idempotency.idempotency;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

/**
 * One Redis key per idempotency key. SETNX (setIfAbsent) is the atomic
 * "claim this key" operation — a plain GET-then-SET would let two concurrent
 * requests both see "not found" and both proceed.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "idem:rides:";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public String hash(String body) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public IdempotencyDecision begin(String key, String bodyHash) {
        String redisKey = redisKey(key);
        IdempotencyRecord pending = new IdempotencyRecord(IdempotencyRecord.State.IN_PROGRESS, bodyHash, 0, null);

        Boolean acquired = redis.opsForValue().setIfAbsent(redisKey, write(pending), TTL);
        if (Boolean.TRUE.equals(acquired)) {
            return new IdempotencyDecision.Proceed();
        }

        String raw = redis.opsForValue().get(redisKey);
        if (raw == null) {
            return new IdempotencyDecision.InProgress();
        }

        IdempotencyRecord existing = read(raw);
        if (!existing.bodyHash().equals(bodyHash)) {
            return new IdempotencyDecision.KeyReused();
        }
        if (existing.state() == IdempotencyRecord.State.DONE) {
            return new IdempotencyDecision.Replay(existing.status(), existing.body());
        }
        return new IdempotencyDecision.InProgress();
    }

    public void complete(String key, String bodyHash, int status, String body) {
        IdempotencyRecord done = new IdempotencyRecord(IdempotencyRecord.State.DONE, bodyHash, status, body);
        redis.opsForValue().set(redisKey(key), write(done), TTL);
    }

    public void release(String key) {
        redis.delete(redisKey(key));
    }

    private String redisKey(String key) {
        return KEY_PREFIX + key;
    }

    private String write(IdempotencyRecord record) {
        try {
            return mapper.writeValueAsString(record);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private IdempotencyRecord read(String raw) {
        try {
            return mapper.readValue(raw, IdempotencyRecord.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
