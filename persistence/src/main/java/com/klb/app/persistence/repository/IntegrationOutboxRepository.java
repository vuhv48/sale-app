package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.IntegrationOutbox;
import com.klb.app.persistence.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IntegrationOutboxRepository extends JpaRepository<IntegrationOutbox, UUID> {

	@Query("select o.id from IntegrationOutbox o where o.status = :status order by o.createdAt asc")
	List<UUID> findIdsByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status, Pageable pageable);
}
