package com.sharespace.sharespace_server.matching.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

	// Matching을 조회할 때 Product와 Place를 한꺼번에 가져오는 메서드
	@Query("SELECT m FROM Matching m JOIN FETCH m.product JOIN FETCH m.place")
	List<Matching> findAllWithProductAndPlace();

	@Query("SELECT m "
		+ "from Matching m "
		+ "JOIN FETCH m.product p "
		+ "where p.user.id = :userId")
	List<Matching> findMatchingWithProductByUserId(Long userId);

	@Query("SELECT m "
		+ "from Matching m "
		+ "JOIN FETCH m.place p "
		+ "where p.user.id = :userId")
	List<Matching> findMatchingWithPlaceByUserId(Long userId);


	// Matching을 조회할 때 Product까지만 가져오는 메서드
	@Query("SELECT m FROM Matching m JOIN FETCH m.product")
	List<Matching> findAllWithProduct();

	// Matching을 조회할 때 Place까지만 가져오는 메서드
	@Query("SELECT m FROM Matching m JOIN FETCH m.place")
	List<Matching> findAllWithPlace();

	// status가 특정 값이고, startDate + confirmDays가 오늘 날짜(today) 이전 또는 같은 Matching 엔티티들을 조회
	@Query("SELECT m FROM Matching m WHERE m.status = :status AND m.startDate + :confirmDays <= :today")
	List<Matching> findEligibleForNotification(Status status, int confirmDays, LocalDateTime today);
}
