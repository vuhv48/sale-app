package com.klb.app.application.service.impl.sales;

import com.klb.app.application.service.sales.CustomerResponse;
import com.klb.app.application.service.sales.CustomerService;
import com.klb.app.application.service.sales.SalesSimplePageResponse;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.CustomerEntity;
import com.klb.app.persistence.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

	public static final String CUSTOMER_LIST_CACHE = "customers:list-page";
	public static final String CUSTOMER_DETAIL_CACHE = "customers:detail";

	private final CustomerRepository customerRepository;

	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = CUSTOMER_LIST_CACHE, key = "T(com.klb.app.application.service.impl.sales.CustomerServiceImpl).listPageCacheKey(#pageable)")
	public SalesSimplePageResponse<CustomerResponse> listPage(Pageable pageable) {
		Page<CustomerEntity> page = customerRepository.findAllActive(pageable);
		return new SalesSimplePageResponse<>(
				page.getContent().stream().map(CustomerServiceImpl::toResponse).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = CUSTOMER_DETAIL_CACHE, key = "#customerId")
	public CustomerResponse getById(UUID customerId) {
		CustomerEntity customer = customerRepository.findActiveById(customerId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Customer not found: " + customerId));
		return toResponse(customer);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = CUSTOMER_LIST_CACHE, allEntries = true)
	public CustomerResponse create(String customerCode, String name, String phone, String email, String taxCode, String addressLine) {
		String code = normalizeRequired(customerCode, "customerCode");
		if (customerRepository.existsByCustomerCode(code)) {
			throw new DomainException(ErrorStatus.DATA_INTEGRITY, "Customer code already exists: " + code);
		}
		CustomerEntity customer = new CustomerEntity();
		customer.setCustomerCode(code);
		customer.setName(normalizeRequired(name, "name"));
		customer.setPhone(normalizeOptional(phone));
		customer.setEmail(normalizeOptional(email));
		customer.setTaxCode(normalizeOptional(taxCode));
		customer.setAddressLine(normalizeOptional(addressLine));
		customerRepository.save(customer);
		return toResponse(customer);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = CUSTOMER_LIST_CACHE, allEntries = true),
			@CacheEvict(cacheNames = CUSTOMER_DETAIL_CACHE, key = "#customerId")
	})
	public CustomerResponse update(UUID customerId, String name, String phone, String email, String taxCode, String addressLine, Boolean active) {
		CustomerEntity customer = customerRepository.findActiveById(customerId)
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Customer not found: " + customerId));
		if (StringUtils.hasText(name)) {
			customer.setName(name.trim());
		}
		customer.setPhone(normalizeOptional(phone));
		customer.setEmail(normalizeOptional(email));
		customer.setTaxCode(normalizeOptional(taxCode));
		customer.setAddressLine(normalizeOptional(addressLine));
		if (active != null) {
			customer.setActive(active);
		}
		return toResponse(customer);
	}

	public static String listPageCacheKey(Pageable pageable) {
		String actor = currentActorKey();
		String sort = pageable.getSort().isSorted() ? pageable.getSort().toString() : "UNSORTED";
		return actor
				+ ":" + pageable.getPageNumber()
				+ ":" + pageable.getPageSize()
				+ ":" + sort.replace(':', '_').replace(' ', '_');
	}

	private static String currentActorKey() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			return "anonymous";
		}
		return authentication.getName().trim().toLowerCase().replace(':', '_');
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

	private static CustomerResponse toResponse(CustomerEntity entity) {
		return new CustomerResponse(
				entity.getId(),
				entity.getCustomerCode(),
				entity.getName(),
				entity.getPhone(),
				entity.getEmail(),
				entity.getTaxCode(),
				entity.getAddressLine(),
				entity.isActive(),
				entity.getCreatedAt());
	}

}
