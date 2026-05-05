package com.project.DuAnTotNghiep.dto.BillReturn;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillReturnDetailDto {
    private Long id;
    private String billCode;
    private Long billId;
    private LocalDateTime returnDate;
    private CustomerDto customerDto;
    private String billReturnCode;
    private Integer percentFeeExchange;
    private Double returnMoney;
    private int billReturnStatus;
    // Danh sách hàng đổi
    private List<ReturnProductDto> returnProductDtos;
    // Danh sách hàng trả
    private List<RefundProductDto> refundProductDtos;
}
