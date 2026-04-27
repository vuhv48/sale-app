package com.klb.app.application.service.impl.document;

import com.klb.app.application.service.document.DocumentService;
import com.klb.app.persistence.entity.DocumentEntity;
import com.klb.app.persistence.entity.DocumentTypeEntity;
import com.klb.app.persistence.entity.DocumentVersionEntity;
import com.klb.app.persistence.repository.DocumentRepository;
import com.klb.app.persistence.repository.DocumentTypeRepository;
import com.klb.app.persistence.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

	private final DocumentRepository documentRepository;
	private final DocumentTypeRepository documentTypeRepository;
	private final DocumentVersionRepository documentVersionRepository;

	@Override
	@Transactional
	public UUID saveUploadedDocument(
			String documentTypeCode,
			String provider,
			String bucket,
			String filePath,
			String originalFilename,
			String mimeType,
			long sizeBytes
	) {
		String typeCode = StringUtils.hasText(documentTypeCode) ? documentTypeCode.trim().toUpperCase() : "GENERIC_FILE";
		DocumentTypeEntity documentType = documentTypeRepository.findActiveByCode(typeCode).orElseGet(() -> {
			DocumentTypeEntity t = new DocumentTypeEntity();
			t.setCode(typeCode);
			t.setName(typeCode);
			t.setActive(true);
			t.setRequireVerification(false);
			return documentTypeRepository.save(t);
		});

		DocumentEntity e = new DocumentEntity();
		e.setProvider(StringUtils.hasText(provider) ? provider.trim() : "minio");
		e.setBucket(bucket);
		e.setFilePath(filePath);
		e.setOriginalFilename(StringUtils.hasText(originalFilename) ? originalFilename.trim() : null);
		e.setMimeType(StringUtils.hasText(mimeType) ? mimeType.trim() : null);
		e.setSizeBytes(sizeBytes);
		e.setStatus("UPLOADED");
		e.setDocumentTypeId(documentType.getId());
		documentRepository.save(e);

		DocumentVersionEntity version = new DocumentVersionEntity();
		version.setDocumentId(e.getId());
		version.setVersionNo(1);
		version.setStorageProvider(e.getProvider());
		version.setBucket(bucket);
		version.setFilePath(filePath);
		version.setOriginalFilename(e.getOriginalFilename());
		version.setMimeType(e.getMimeType());
		version.setSizeBytes(sizeBytes);
		version.setUploadStatus("UPLOADED");
		documentVersionRepository.save(version);

		e.setCurrentVersionId(version.getId());
		documentRepository.save(e);
		return e.getId();
	}
}
