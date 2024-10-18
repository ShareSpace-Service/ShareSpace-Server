package com.sharespace.sharespace_server.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	@EntityGraph(attributePaths = {"matchings.place"})
	List<Product> findAllByUserId(Long userId);
}
