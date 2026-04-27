package com.klb.app.application.service.impl.document;

import com.klb.app.application.service.document.DocumentService;
import com.klb.app.persistence.entity.DocumentEntity;
import com.klb.app.persistence.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

	private final DocumentRepository documentRepository;

	@Override
	@Transactional
	public UUID saveUploadedDocument(
			String provider,
			String bucket,
			String filePath,
			String originalFilename,
			String mimeType,
			long sizeBytes
	) {
		DocumentEntity e = new DocumentEntity();
		e.setProvider(StringUtils.hasText(provider) ? provider.trim() : "minio");
		e.setBucket(bucket);
		e.setFilePath(filePath);
		e.setOriginalFilename(StringUtils.hasText(originalFilename) ? originalFilename.trim() : null);
		e.setMimeType(StringUtils.hasText(mimeType) ? mimeType.trim() : null);
		e.setSizeBytes(sizeBytes);
		e.setStatus("UPLOADED");
		documentRepository.save(e);
		return e.getId();
	}
}
