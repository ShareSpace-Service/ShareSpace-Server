package com.sharespace.sharespace_server.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.place.entity.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
}
