package com.project.DuAnTotNghiep.dto.Product;

import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.entity.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {
    private Long id;
    private int quantity;
    private String barcode;
    private double price;
    private int status;
    private Long productId;
    private Size size;
    private Color color;
    private Double discountedPrice;
}
