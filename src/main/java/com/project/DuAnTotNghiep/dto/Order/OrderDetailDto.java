package com.project.DuAnTotNghiep.dto.Order;

import com.project.DuAnTotNghiep.dto.Product.ProductDetailDto;
import lombok.Data;

@Data
public class OrderDetailDto {
    private Integer quantity;
    private Long productDetailId;
}
