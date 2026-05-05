package com.project.DuAnTotNghiep.dto.Refund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRefundDto {
    private String billCode;
    private String billReturnCode;
    private String customerName;
    private int status;
    private String startDate;
    private String endDate;
}
