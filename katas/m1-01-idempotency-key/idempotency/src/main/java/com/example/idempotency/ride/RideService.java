package com.example.idempotency.ride;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RideService {

    private final List<Ride> rides = new CopyOnWriteArrayList<>();

    public Ride createRide(RideRequest request) {
        Ride ride = new Ride(UUID.randomUUID().toString(), request.pickup(), request.dropoff());
        rides.add(ride);
        return ride;
    }

    public List<Ride> findAll() {
        return List.copyOf(rides);
    }
}
