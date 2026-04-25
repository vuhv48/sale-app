package com.klb.app.web.controller;

import com.klb.app.application.service.sales.SalesOrderPageResponse;
import com.klb.app.application.service.sales.SalesOrderResponse;
import com.klb.app.application.service.sales.SalesOrderService;
import com.klb.app.web.dto.CreateSalesOrderRequest;
import com.klb.app.web.dto.UpdateOrderStatusRequest;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class SalesOrderController {

	private final SalesOrderService salesOrderService;

	@GetMapping
	public SalesOrderPageResponse list(@PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
		return salesOrderService.listPage(pageable);
	}

	@GetMapping("/{orderId}")
	public SalesOrderResponse detail(@PathVariable UUID orderId) {
		return salesOrderService.getDetail(orderId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SalesOrderResponse create(@Valid @RequestBody CreateSalesOrderRequest body) {
		return salesOrderService.create(
				body.customerId(),
				body.note(),
				body.items().stream().map(i -> new SalesOrderService.CreateOrderItemInput(i.skuId(), i.quantity())).toList());
	}

	@PatchMapping("/{orderId}/confirm")
	public SalesOrderResponse confirm(@PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest body) {
		return salesOrderService.confirm(orderId, body.reason());
	}

	@PatchMapping("/{orderId}/cancel")
	public SalesOrderResponse cancel(@PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest body) {
		return salesOrderService.cancel(orderId, body.reason());
	}
}
