package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.SalesOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrderEntity, UUID> {

	@Query("select case when count(o) > 0 then true else false end from SalesOrderEntity o where o.orderNo = :orderNo and o.isDeleted = false")
	boolean existsByOrderNo(@Param("orderNo") String orderNo);

	@Query("select o from SalesOrderEntity o where o.id = :id and o.isDeleted = false")
	Optional<SalesOrderEntity> findActiveById(@Param("id") UUID id);

	@Query("select o from SalesOrderEntity o where o.isDeleted = false order by o.orderDate desc")
	Page<SalesOrderEntity> findAllActive(Pageable pageable);
}
