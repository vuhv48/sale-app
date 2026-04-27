package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

	@Query(value = """
		SELECT d.id
		FROM documents d
		WHERE d.current_version_id IS NULL
		  AND d.is_deleted = FALSE
		  AND mod(abs(hashtext(d.id::text)), :workers) = :worker
		ORDER BY d.created_at ASC, d.id ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<UUID> findMissingCurrentVersionIdsByPartition(
			@Param("limit") int limit,
			@Param("workers") int workers,
			@Param("worker") int worker
	);

	@Modifying
	@Query(value = """
		UPDATE documents d
		SET current_version_id = v.id,
		    updated_at = now()
		FROM document_versions v
		WHERE d.id = :documentId
		  AND d.current_version_id IS NULL
		  AND v.document_id = d.id
		  AND v.version_no = 1
		""", nativeQuery = true)
	int attachVersionOneAsCurrent(@Param("documentId") UUID documentId);
}
