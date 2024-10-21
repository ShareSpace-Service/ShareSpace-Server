package com.sharespace.sharespace_server.matching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.matching.entity.Matching;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
	Optional<Matching> findAllByPlaceUserIdAndStatusIn(Long id, List<Status> pending);
	Optional<Matching> findAllByProductUserIdAndStatusIn(Long id, List<Status> pending);
	List<Matching> findAllByProductIdInAndPlaceId(List<Long> productIds, Long placeId);
	Optional<Matching> findByProductIdAndPlaceId(Long productId, Long placeId);
	List<Matching> findAllByStatus(Status status);

	Matching findByProductId(Long ProductId);
}
