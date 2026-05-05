package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByCode(String code);
}
