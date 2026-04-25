package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

	@Query("select case when count(c) > 0 then true else false end from CustomerEntity c where c.customerCode = :customerCode and c.isDeleted = false")
	boolean existsByCustomerCode(@Param("customerCode") String customerCode);

	@Query("select c from CustomerEntity c where c.id = :id and c.isDeleted = false")
	Optional<CustomerEntity> findActiveById(@Param("id") UUID id);

	@Query("select c from CustomerEntity c where c.isDeleted = false order by c.customerCode asc")
	Page<CustomerEntity> findAllActive(Pageable pageable);
}
