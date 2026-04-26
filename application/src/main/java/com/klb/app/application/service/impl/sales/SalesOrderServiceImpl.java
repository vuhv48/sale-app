package com.klb.app.application.service.impl.sales;

import com.klb.app.application.service.sales.SalesOrderItemResponse;
import com.klb.app.application.service.sales.SalesOrderPageResponse;
import com.klb.app.application.service.sales.SalesOrderResponse;
import com.klb.app.application.service.mail.OrderCreatedMailService;
import com.klb.app.application.service.sales.SalesOrderService;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.CustomerEntity;
import com.klb.app.persistence.entity.ProductSkuEntity;
import com.klb.app.persistence.entity.SalesOrderEntity;
import com.klb.app.persistence.entity.SalesOrderItemEntity;
import com.klb.app.persistence.entity.SalesOrderStatus;
import com.klb.app.persistence.entity.SalesOrderStatusHistoryEntity;
import com.klb.app.persistence.repository.CustomerRepository;
import com.klb.app.persistence.repository.ProductSkuRepository;
import com.klb.app.persistence.repository.SalesOrderItemRepository;
import com.klb.app.persistence.repository.SalesOrderRepository;
import com.klb.app.persistence.repository.SalesOrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

	private static final DateTimeFormatter ORDER_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss").withZone(ZoneOffset.UTC);

	private final SalesOrderRepository salesOrderRepository;
	private final SalesOrderItemRepository salesOrderItemRepository;
	private final SalesOrderStatusHistoryRepository salesOrderStatusHistoryRepository;
	private final CustomerRepository customerRepository;
	private final ProductSkuRepository productSkuRepository;
	private final OrderCreatedMailService orderCreatedMailService;

	@Override
	@Transactional(readOnly = true)
	public SalesOrderPageResponse listPage(Pageable pageable) {
		Page<SalesOrderEntity> page = salesOrderRepository.findAllActive(pageable);
		List<SalesOrderResponse> content = page.getContent().stream()
				.map(order -> toResponse(order, salesOrderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())))
				.toList();
		return new SalesOrderPageResponse(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
	}

	@Override
	@Transactional(readOnly = true)
	public SalesOrderResponse getDetail(UUID orderId) {
		SalesOrderEntity order = getOrder(orderId);
		List<SalesOrderItemEntity> items = salesOrderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
		return toResponse(order, items);
	}

	@Override
	@Transactional
	public SalesOrderResponse create(UUID customerId, String note, List<CreateOrderItemInput> items) {
		if (items == null || items.isEmpty()) {
			throw new DomainException(ErrorStatus.VALIDATION_ERROR, "Order must contain at least one item");
		}
		CustomerEntity customer = customerRepository.findActiveById(customerId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Customer not found: " + customerId));
		SalesOrderEntity order = new SalesOrderEntity();
		order.setCustomer(customer);
		order.setOrderStatus(SalesOrderStatus.NEW);
		order.setOrderNo(generateOrderNo());
		order.setNote(StringUtils.hasText(note) ? note.trim() : null);
		salesOrderRepository.save(order);

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (CreateOrderItemInput input : items) {
			ProductSkuEntity sku = productSkuRepository.findActiveById(input.skuId())
					.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "SKU not found: " + input.skuId()));
			if (!sku.isActive()) {
				throw new DomainException(ErrorStatus.ILLEGAL_STATE, "SKU is inactive: " + sku.getSkuCode());
			}
			if (input.quantity() == null || input.quantity().signum() <= 0) {
				throw new DomainException(ErrorStatus.VALIDATION_ERROR, "Quantity must be > 0");
			}
			SalesOrderItemEntity item = new SalesOrderItemEntity();
			item.setOrder(order);
			item.setSku(sku);
			item.setQuantity(input.quantity());
			item.setUnitPrice(sku.getUnitPrice());
			item.setLineTotal(sku.getUnitPrice().multiply(input.quantity()));
			totalAmount = totalAmount.add(item.getLineTotal());
			salesOrderItemRepository.save(item);
		}
		order.setTotalAmount(totalAmount);
		appendHistory(order, null, SalesOrderStatus.NEW, "Create order");
		orderCreatedMailService.enqueueOrderCreatedEmail(
				order.getId(),
				order.getOrderNo(),
				customer.getName(),
				customer.getEmail(),
				totalAmount);
		return getDetail(order.getId());
	}

	@Override
	@Transactional
	public SalesOrderResponse confirm(UUID orderId, String reason) {
		SalesOrderEntity order = getOrder(orderId);
		if (order.getOrderStatus() != SalesOrderStatus.NEW) {
			throw new DomainException(ErrorStatus.ILLEGAL_STATE, "Only NEW order can be confirmed");
		}
		SalesOrderStatus from = order.getOrderStatus();
		order.setOrderStatus(SalesOrderStatus.CONFIRMED);
		appendHistory(order, from, SalesOrderStatus.CONFIRMED, reason);
		return getDetail(orderId);
	}

	@Override
	@Transactional
	public SalesOrderResponse cancel(UUID orderId, String reason) {
		SalesOrderEntity order = getOrder(orderId);
		if (order.getOrderStatus() == SalesOrderStatus.CANCELED) {
			throw new DomainException(ErrorStatus.ILLEGAL_STATE, "Order already canceled");
		}
		SalesOrderStatus from = order.getOrderStatus();
		order.setOrderStatus(SalesOrderStatus.CANCELED);
		appendHistory(order, from, SalesOrderStatus.CANCELED, reason);
		return getDetail(orderId);
	}

	private String generateOrderNo() {
		for (int i = 0; i < 10; i++) {
			String candidate = "SO" + ORDER_TIME.format(Instant.now()) + Integer.toHexString((int) (Math.random() * 256)).toUpperCase();
			if (!salesOrderRepository.existsByOrderNo(candidate)) {
				return candidate;
			}
		}
		throw new DomainException(ErrorStatus.ILLEGAL_STATE, "Cannot generate unique order number");
	}

	private void appendHistory(SalesOrderEntity order, SalesOrderStatus from, SalesOrderStatus to, String reason) {
		SalesOrderStatusHistoryEntity history = new SalesOrderStatusHistoryEntity();
		history.setOrder(order);
		history.setFromStatus(from);
		history.setToStatus(to);
		history.setChangedReason(StringUtils.hasText(reason) ? reason.trim() : null);
		history.setChangedAt(Instant.now());
		salesOrderStatusHistoryRepository.save(history);
	}

	private SalesOrderEntity getOrder(UUID orderId) {
		return salesOrderRepository.findActiveById(orderId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Order not found: " + orderId));
	}

	private static SalesOrderResponse toResponse(SalesOrderEntity order, List<SalesOrderItemEntity> items) {
		return new SalesOrderResponse(
				order.getId(),
				order.getOrderNo(),
				order.getCustomer().getId(),
				order.getCustomer().getCustomerCode(),
				order.getCustomer().getName(),
				order.getOrderStatus(),
				order.getOrderDate(),
				order.getNote(),
				order.getTotalAmount(),
				items.stream().map(SalesOrderServiceImpl::toItemResponse).toList(),
				order.getCreatedAt());
	}

	private static SalesOrderItemResponse toItemResponse(SalesOrderItemEntity item) {
		return new SalesOrderItemResponse(
				item.getId(),
				item.getSku().getId(),
				item.getSku().getSkuCode(),
				item.getSku().getSkuName(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getLineTotal());
	}
}
