package com.sharespace.sharespace_server.matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.matching.entity.Matching;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
}
