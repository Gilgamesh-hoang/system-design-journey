package com.example.idempotency.idempotency;

record IdempotencyRecord(State state, String bodyHash, int status, String body) {

    enum State {
        IN_PROGRESS, DONE
    }
}
