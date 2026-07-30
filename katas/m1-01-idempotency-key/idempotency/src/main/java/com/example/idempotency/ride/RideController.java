package com.example.idempotency.ride;

import com.example.idempotency.idempotency.IdempotencyDecision;
import com.example.idempotency.idempotency.IdempotencyService;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper mapper;

    @PostMapping
    public ResponseEntity<String> createRide(
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @RequestBody RideRequest request) {

//        if (idempotencyKey == null || idempotencyKey.isBlank()) {
//            return json(HttpStatus.BAD_REQUEST, "{\"error\":\"Idempotency-Key header is required\"}");
//        }

        String bodyHash = idempotencyService.hash(mapper.writeValueAsString(request));
        IdempotencyDecision decision = idempotencyService.begin(idempotencyKey, bodyHash);

        if (decision instanceof IdempotencyDecision.Replay replay) {
            return json(HttpStatus.valueOf(replay.status()), replay.body());
        }
        if (decision instanceof IdempotencyDecision.InProgress) {
            return json(HttpStatus.CONFLICT, "{\"error\":\"request with this idempotency key is still processing, retry shortly\"}");
        }
        if (decision instanceof IdempotencyDecision.KeyReused) {
            return json(HttpStatus.CONFLICT, "{\"error\":\"Idempotency-Key already used with a different request body\"}");
        }

        try {
            Ride ride = rideService.createRide(request);
            String responseBody = mapper.writeValueAsString(ride);
            idempotencyService.complete(idempotencyKey, bodyHash, HttpStatus.CREATED.value(), responseBody);
            return json(HttpStatus.CREATED, responseBody);
        } catch (RuntimeException e) {
            idempotencyService.release(idempotencyKey);
            throw e;
        }
    }

    private ResponseEntity<String> json(HttpStatus status, String body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
