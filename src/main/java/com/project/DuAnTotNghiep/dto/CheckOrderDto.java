package com.project.DuAnTotNghiep.dto;

import com.project.DuAnTotNghiep.dto.Product.ProductDetailDto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckOrderDto {
    private Long productDetailId;
    private String quantity;
}
