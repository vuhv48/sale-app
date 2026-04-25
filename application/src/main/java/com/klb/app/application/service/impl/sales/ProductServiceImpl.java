package com.klb.app.application.service.impl.sales;

import com.klb.app.application.service.sales.ProductResponse;
import com.klb.app.application.service.sales.ProductService;
import com.klb.app.application.service.sales.ProductSkuResponse;
import com.klb.app.application.service.sales.SalesSimplePageResponse;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.ProductEntity;
import com.klb.app.persistence.entity.ProductSkuEntity;
import com.klb.app.persistence.repository.ProductRepository;
import com.klb.app.persistence.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductSkuRepository productSkuRepository;

	@Override
	@Transactional(readOnly = true)
	public SalesSimplePageResponse<ProductResponse> listPage(Pageable pageable) {
		Page<ProductEntity> page = productRepository.findAllActive(pageable);
		return new SalesSimplePageResponse<>(
				page.getContent().stream().map(ProductServiceImpl::toResponse).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

	@Override
	@Transactional
	public ProductResponse create(String productCode, String name, String description) {
		String code = normalizeRequired(productCode, "productCode");
		if (productRepository.existsByProductCode(code)) {
			throw new DomainException(ErrorStatus.DATA_INTEGRITY, "Product code already exists: " + code);
		}
		ProductEntity product = new ProductEntity();
		product.setProductCode(code);
		product.setName(normalizeRequired(name, "name"));
		product.setDescription(normalizeOptional(description));
		productRepository.save(product);
		return toResponse(product);
	}

	@Override
	@Transactional
	public ProductResponse update(UUID productId, String name, String description, Boolean active) {
		ProductEntity product = productRepository.findActiveById(productId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Product not found: " + productId));
		if (StringUtils.hasText(name)) {
			product.setName(name.trim());
		}
		product.setDescription(normalizeOptional(description));
		if (active != null) {
			product.setActive(active);
		}
		return toResponse(product);
	}

	@Override
	@Transactional
	public ProductSkuResponse createSku(UUID productId, String skuCode, String skuName, BigDecimal unitPrice) {
		ProductEntity product = productRepository.findActiveById(productId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Product not found: " + productId));
		String code = normalizeRequired(skuCode, "skuCode");
		if (productSkuRepository.existsBySkuCode(code)) {
			throw new DomainException(ErrorStatus.DATA_INTEGRITY, "SKU code already exists: " + code);
		}
		ProductSkuEntity sku = new ProductSkuEntity();
		sku.setProduct(product);
		sku.setSkuCode(code);
		sku.setSkuName(normalizeRequired(skuName, "skuName"));
		sku.setUnitPrice(normalizePrice(unitPrice, "unitPrice"));
		productSkuRepository.save(sku);
		return toSkuResponse(sku);
	}

	@Override
	@Transactional
	public ProductSkuResponse updateSku(UUID skuId, String skuName, BigDecimal unitPrice, Boolean active) {
		ProductSkuEntity sku = productSkuRepository.findActiveById(skuId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "SKU not found: " + skuId));
		if (StringUtils.hasText(skuName)) {
			sku.setSkuName(skuName.trim());
		}
		if (unitPrice != null) {
			sku.setUnitPrice(normalizePrice(unitPrice, "unitPrice"));
		}
		if (active != null) {
			sku.setActive(active);
		}
		return toSkuResponse(sku);
	}

	private static BigDecimal normalizePrice(BigDecimal value, String field) {
		if (value == null || value.signum() < 0) {
			throw new DomainException(ErrorStatus.VALIDATION_ERROR, "Field must be >= 0: " + field);
		}
		return value;
	}

	private static String normalizeRequired(String value, String field) {
		if (!StringUtils.hasText(value)) {
			throw new DomainException(ErrorStatus.VALIDATION_ERROR, "Field is required: " + field);
		}
		return value.trim();
	}

	private static String normalizeOptional(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private static ProductResponse toResponse(ProductEntity entity) {
		return new ProductResponse(
				entity.getId(),
				entity.getProductCode(),
				entity.getName(),
				entity.getDescription(),
				entity.isActive(),
				entity.getCreatedAt());
	}

	private static ProductSkuResponse toSkuResponse(ProductSkuEntity entity) {
		return new ProductSkuResponse(
				entity.getId(),
				entity.getProduct().getId(),
				entity.getSkuCode(),
				entity.getSkuName(),
				entity.getUnitPrice(),
				entity.isActive(),
				entity.getCreatedAt());
	}
}
