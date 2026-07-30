package com.example.idempotency.idempotency;

public sealed interface IdempotencyDecision {

    record Proceed() implements IdempotencyDecision {
    }

    record Replay(int status, String body) implements IdempotencyDecision {
    }

    record InProgress() implements IdempotencyDecision {
    }

    record KeyReused() implements IdempotencyDecision {
    }
}
