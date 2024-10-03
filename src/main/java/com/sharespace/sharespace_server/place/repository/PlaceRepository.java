package com.sharespace.sharespace_server.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.place.entity.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
	Optional<Place> findById(Long placeId);
	Optional<Place> findByUserId(Long userId);
	List<Place> findAllByUserId(Long userId);
}
