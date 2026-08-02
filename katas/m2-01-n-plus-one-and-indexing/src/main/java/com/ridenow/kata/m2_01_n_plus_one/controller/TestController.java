package com.ridenow.kata.m2_01_n_plus_one.controller;

import com.ridenow.kata.m2_01_n_plus_one.model.Trip;
import com.ridenow.kata.m2_01_n_plus_one.repository.TripRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TripRepository tripRepository;

    public TestController(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @GetMapping("/n-plus-one")
    public List<String> testNPlusOne() {
        // Query 1: Lấy 100 trips (LIMIT 100)
        List<Trip> trips = tripRepository.findAllByOrderByIdDesc(PageRequest.of(0, 100));
        
        // Loop này sẽ sinh ra 100 queries con do FetchType.LAZY 
        return trips.stream()
                .map(t -> "Trip: " + t.getId() + " - Driver: " + t.getDriver().getName())
                .collect(Collectors.toList());
    }

    @GetMapping("/n-plus-one/fixed")
    public List<String> testNPlusOneFixed() {
        // Query 1: Lấy 100 trips + JOIN FETCH Driver
        List<Trip> trips = tripRepository.findWithDriverByOrderByIdDesc(PageRequest.of(0, 100));
        
        // Loop này hoàn toàn KHÔNG sinh thêm query vì Driver đã nằm trong RAM
        return trips.stream()
                .map(t -> "Trip: " + t.getId() + " - Driver: " + t.getDriver().getName())
                .collect(Collectors.toList());
    }
}
