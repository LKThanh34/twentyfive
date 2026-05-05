package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.BillReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRepository extends JpaRepository<BillReturn, Long> {
}
