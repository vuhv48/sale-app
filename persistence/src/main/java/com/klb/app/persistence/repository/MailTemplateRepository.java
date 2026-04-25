package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {

	Optional<MailTemplate> findByCodeAndActiveTrue(String code);
}
