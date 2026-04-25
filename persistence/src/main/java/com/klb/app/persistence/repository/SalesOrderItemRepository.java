package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.SalesOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItemEntity, UUID> {

	List<SalesOrderItemEntity> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
