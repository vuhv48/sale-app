package com.klb.app.web.controller;

import com.klb.app.application.service.sales.ProductResponse;
import com.klb.app.application.service.sales.ProductService;
import com.klb.app.application.service.sales.ProductSkuResponse;
import com.klb.app.application.service.sales.SalesSimplePageResponse;
import com.klb.app.web.dto.CreateProductRequest;
import com.klb.app.web.dto.CreateSkuRequest;
import com.klb.app.web.dto.UpdateProductRequest;
import com.klb.app.web.dto.UpdateSkuRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public SalesSimplePageResponse<ProductResponse> list(@PageableDefault(size = 20, sort = "productCode") Pageable pageable) {
		return productService.listPage(pageable);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody CreateProductRequest body) {
		return productService.create(body.productCode(), body.name(), body.description());
	}

	@PatchMapping("/{productId}")
	public ProductResponse update(@PathVariable UUID productId, @Valid @RequestBody UpdateProductRequest body) {
		return productService.update(productId, body.name(), body.description(), body.active());
	}

	@PostMapping("/{productId}/skus")
	@ResponseStatus(HttpStatus.CREATED)
	public ProductSkuResponse createSku(@PathVariable UUID productId, @Valid @RequestBody CreateSkuRequest body) {
		return productService.createSku(productId, body.skuCode(), body.skuName(), body.unitPrice());
	}

	@PatchMapping("/skus/{skuId}")
	public ProductSkuResponse updateSku(@PathVariable UUID skuId, @Valid @RequestBody UpdateSkuRequest body) {
		return productService.updateSku(skuId, body.skuName(), body.unitPrice(), body.active());
	}
}
