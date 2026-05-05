package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.ProductDiscount.ProductDiscountCreateDto;
import com.project.DuAnTotNghiep.dto.ProductDiscount.ProductDiscountDto;
import com.project.DuAnTotNghiep.entity.ProductDiscount;

import java.util.List;

public interface ProductDiscountService {
    List<ProductDiscount> getAllProductDiscount();

    ProductDiscountDto updateCloseProductDiscount(Long discountId, boolean closed);

    List<ProductDiscountDto> createProductDiscountMultiple(ProductDiscountCreateDto productDiscountCreateDto);
}
