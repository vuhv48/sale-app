package com.klb.app.web.controller;

import com.klb.app.application.service.sales.CustomerResponse;
import com.klb.app.application.service.sales.CustomerService;
import com.klb.app.application.service.sales.SalesSimplePageResponse;
import com.klb.app.web.dto.CreateCustomerRequest;
import com.klb.app.web.dto.UpdateCustomerRequest;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	@GetMapping
	public SalesSimplePageResponse<CustomerResponse> list(@PageableDefault(size = 20, sort = "customerCode") Pageable pageable) {
		return customerService.listPage(pageable);
	}

	@GetMapping("/{customerId}")
	public CustomerResponse getById(@PathVariable UUID customerId) {
		return customerService.getById(customerId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest body) {
		return customerService.create(
				body.customerCode(),
				body.name(),
				body.phone(),
				body.email(),
				body.taxCode(),
				body.addressLine());
	}

	@PatchMapping("/{customerId}")
	public CustomerResponse update(@PathVariable UUID customerId, @Valid @RequestBody UpdateCustomerRequest body) {
		return customerService.update(
				customerId,
				body.name(),
				body.phone(),
				body.email(),
				body.taxCode(),
				body.addressLine(),
				body.active());
	}
}
