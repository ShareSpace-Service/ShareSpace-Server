package com.sharespace.sharespace_server.matching.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.matching.entity.Matching;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
	List<Matching> findAllByProductIdInAndPlaceId(List<Long> productIds, Long placeId);
	Optional<Matching> findByProductIdAndPlaceId(Long productId, Long placeId);
	List<Matching> findAllByStatus(Status status);

	Matching findByProductId(Long ProductId);

	@Query("SELECT COUNT(m) FROM Matching m WHERE m.place.id IN :placeIds AND m.status = :status")
	Integer countByPlaceIdInAndStatus(@Param("placeIds") List<Long> placeIds, @Param("status") Status status);

	@Query("SELECT m FROM Matching m WHERE m.status = :status AND m.expiryDate BETWEEN CURRENT_TIMESTAMP AND :threeDaysAfter ORDER BY m.expiryDate ASC")
	List<Matching> findUpcomingMatches(@Param("status") Status status, @Param("threeDaysAfter") LocalDateTime threeDaysAfter);

	// Matching을 조회할 때 Product와 Place를 한꺼번에 가져오는 메서드
	@Query("SELECT m FROM Matching m JOIN FETCH m.product JOIN FETCH m.place")
	List<Matching> findAllWithProductAndPlace();

	@Query("SELECT m "
		+ "from Matching m "
		+ "JOIN FETCH m.product p "
		+ "where p.user.id = :userId and not m.status='COMPLETED'")
	List<Matching> findMatchingWithProductByUserId(Long userId);

	@Query("SELECT m "
		+ "from Matching m "
		+ "JOIN FETCH m.place p "
		+ "where p.user.id = :userId  and not m.status='COMPLETED'")
	List<Matching> findMatchingWithPlaceByUserId(Long userId);

	// Matching + Product 찾기 By Status
	@Query("SELECT m "
		+ "from Matching m "
		+ "JOIN FETCH m.product p "
		+ "where p.user.id = :userId and m.status = :status")
	List<Matching> findMatchingWithProductByUserIdAndStatus(Long userId, Status status);

	// Matching + Place 찾기 By Status
	@Query("SELECT m "
		+ "from Matching m "
		+ "JOIN FETCH m.place p "
		+ "where p.user.id = :userId and m.status = :status")
	List<Matching> findMatchingWithPlaceByUserIdAndStatus(Long userId, Status status);


	// Matching을 조회할 때 Product까지만 가져오는 메서드
	@Query("SELECT m FROM Matching m JOIN FETCH m.product")
	List<Matching> findAllWithProduct();

	// Matching을 조회할 때 Place까지만 가져오는 메서드
	@Query("SELECT m FROM Matching m JOIN FETCH m.place")
	List<Matching> findAllWithPlace();

	// status가 특정 값이고, startDate + confirmDays가 오늘 날짜(today) 이전 또는 같은 Matching 엔티티들을 조회
	@Query("SELECT m FROM Matching m "
		+ "WHERE m.status = :status AND "
		+ "m.startDate <= :targetDate")
	List<Matching> findEligibleForNotification(Status status, LocalDateTime targetDate);

	Optional<Matching> findMatchingByProductIdAndPlaceId(Long productId, Long placeId);

	@Query("SELECT m FROM Matching m "
		+ "JOIN FETCH m.product p "
		+ "WHERE (m.status = 'REJECTED' OR m.status = 'UNASSIGNED') "
		+ "AND p.user.id = :userId")
	List<Matching> findMatchingsWithProductByStatus(Long userId);

	@Query("SELECT m FROM Matching m "
		+ "JOIN FETCH m.place p "
		+ "WHERE (m.status = 'PENDING' OR m.status = 'STORED') "
		+ "AND p.user.id = :userId")
	List<Matching> findAllByPlaceUserIdAndStatusIn(Long userId);

	@Query("SELECT m FROM Matching m "
		+ "JOIN FETCH m.product p "
		+ "WHERE (m.status = 'PENDING' OR m.status = 'STORED') "
		+ "AND p.user.id = :userId")
	List<Matching> findAllByProductUserIdAndStatusIn(Long userId);
}
