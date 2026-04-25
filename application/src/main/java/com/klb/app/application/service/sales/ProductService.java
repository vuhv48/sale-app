package com.klb.app.application.service.sales;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {

	SalesSimplePageResponse<ProductResponse> listPage(Pageable pageable);

	ProductResponse create(String productCode, String name, String description);

	ProductResponse update(UUID productId, String name, String description, Boolean active);

	ProductSkuResponse createSku(UUID productId, String skuCode, String skuName, BigDecimal unitPrice);

	ProductSkuResponse updateSku(UUID skuId, String skuName, BigDecimal unitPrice, Boolean active);
}
