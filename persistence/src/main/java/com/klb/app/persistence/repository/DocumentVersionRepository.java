package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.DocumentVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersionEntity, UUID> {

	@Modifying
	@Query(value = """
		INSERT INTO document_versions (
		    id, document_id, version_no, storage_provider, bucket, file_path,
		    object_version, etag, original_filename, mime_type, size_bytes,
		    upload_status, uploaded_at, is_deleted, created_at, updated_at
		)
		SELECT
		    gen_random_uuid(), d.id, 1, d.provider, d.bucket, d.file_path,
		    NULL, NULL, d.original_filename, d.mime_type, d.size_bytes,
		    'UPLOADED', now(), FALSE, now(), now()
		FROM documents d
		WHERE d.id = :documentId
		  AND NOT EXISTS (
		      SELECT 1
		      FROM document_versions v
		      WHERE v.document_id = d.id
		        AND v.version_no = 1
		  )
		""", nativeQuery = true)
	int insertVersionOneIfMissing(@Param("documentId") UUID documentId);
}

