package com.klb.app.application.service.document;

import java.util.UUID;

public interface DocumentService {

	UUID saveUploadedDocument(
			String documentTypeCode,
			String provider,
			String bucket,
			String filePath,
			String originalFilename,
			String mimeType,
			long sizeBytes
	);
}
