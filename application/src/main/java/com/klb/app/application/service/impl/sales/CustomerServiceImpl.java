package com.klb.app.application.service.impl.sales;

import com.klb.app.application.service.sales.CustomerResponse;
import com.klb.app.application.service.sales.CustomerService;
import com.klb.app.application.service.sales.SalesSimplePageResponse;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.CustomerEntity;
import com.klb.app.persistence.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;

	@Override
	@Transactional(readOnly = true)
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
	@Transactional
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
