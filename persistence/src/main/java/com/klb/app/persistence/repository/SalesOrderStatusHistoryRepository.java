package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.SalesOrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderStatusHistoryRepository extends JpaRepository<SalesOrderStatusHistoryEntity, Long> {
}
