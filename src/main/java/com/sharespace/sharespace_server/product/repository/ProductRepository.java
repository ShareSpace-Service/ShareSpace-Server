package com.sharespace.sharespace_server.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
