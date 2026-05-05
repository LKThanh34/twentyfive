package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}