package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

	@Query("select case when count(p) > 0 then true else false end from ProductEntity p where p.productCode = :productCode and p.isDeleted = false")
	boolean existsByProductCode(@Param("productCode") String productCode);

	@Query("select p from ProductEntity p where p.id = :id and p.isDeleted = false")
	Optional<ProductEntity> findActiveById(@Param("id") UUID id);

	@Query("select p from ProductEntity p where p.isDeleted = false order by p.productCode asc")
	Page<ProductEntity> findAllActive(Pageable pageable);
}
