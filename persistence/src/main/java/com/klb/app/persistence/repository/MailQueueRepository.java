package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.MailQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MailQueueRepository extends JpaRepository<MailQueue, UUID> {

	boolean existsByIdempotencyKey(String idempotencyKey);
}
