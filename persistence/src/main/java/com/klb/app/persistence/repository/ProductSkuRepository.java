package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.ProductSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductSkuRepository extends JpaRepository<ProductSkuEntity, UUID> {

	@Query("select case when count(s) > 0 then true else false end from ProductSkuEntity s where s.skuCode = :skuCode and s.isDeleted = false")
	boolean existsBySkuCode(@Param("skuCode") String skuCode);

	@Query("select s from ProductSkuEntity s where s.id = :id and s.isDeleted = false")
	Optional<ProductSkuEntity> findActiveById(@Param("id") UUID id);
}
