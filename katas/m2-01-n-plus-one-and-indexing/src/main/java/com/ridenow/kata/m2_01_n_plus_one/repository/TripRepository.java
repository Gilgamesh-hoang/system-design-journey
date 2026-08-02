package com.ridenow.kata.m2_01_n_plus_one.repository;

import com.ridenow.kata.m2_01_n_plus_one.model.Trip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"driver"})
    List<Trip> findWithDriverByOrderByIdDesc(Pageable pageable);
}
