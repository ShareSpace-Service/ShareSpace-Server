package com.sharespace.sharespace_server.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.place.entity.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
	Optional<Place> findByUserId(Long userId);

	@Query("SELECT p FROM Place p WHERE p.period >= :period")
	List<Place> findPlacesByPeriod(@Param("period") int period);
}
