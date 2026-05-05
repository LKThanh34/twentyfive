package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.AddressShipping;
import com.project.DuAnTotNghiep.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressShippingRepository extends JpaRepository<AddressShipping, Long> {
    List<AddressShipping> findAllByCustomer_Account_Id(Long accountId);
}
