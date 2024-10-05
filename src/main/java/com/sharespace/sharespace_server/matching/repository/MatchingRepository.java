package com.sharespace.sharespace_server.matching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.matching.entity.Matching;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
	List<Matching> findAllByPlaceIdInAndStatusIn(List<Long> places, List<Status> statuses);
	List<Matching> findAllByProductIdInAndStatusIn(List<Long> product, List<Status> statuses);
	Matching findByProductIdInAndPlaceId(List<Long> product, Long place);

	Optional<Matching> findByProductIdAndPlaceId(Long productId, Long placeId);
}
