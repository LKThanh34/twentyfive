package com.project.DuAnTotNghiep.dto.ProductDiscount;


import lombok.Data;

import java.util.Date;

@Data
public class ProductDiscountDto {
    private Long id;
    private Double discountedAmount;
    private Date startDate;
    private Date endDate;
    private boolean closed;
    private Long productDetailId;
}
