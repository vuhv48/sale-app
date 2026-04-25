package com.klb.app.application.service.sales;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

	SalesSimplePageResponse<CustomerResponse> listPage(Pageable pageable);

	CustomerResponse create(
			String customerCode,
			String name,
			String phone,
			String email,
			String taxCode,
			String addressLine);

	CustomerResponse update(
			UUID customerId,
			String name,
			String phone,
			String email,
			String taxCode,
			String addressLine,
			Boolean active);
}
