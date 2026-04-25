package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoticeEntityRepository extends JpaRepository<NoticeEntity, UUID> {
}
