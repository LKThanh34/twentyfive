package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import com.project.DuAnTotNghiep.entity.ProductDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {
    Page<ProductDiscount> findAllByProductDetailNotNull(Pageable pageable);
    List<ProductDiscount> findAllByProductDetailIn(List<ProductDetail> productDetails);
    ProductDiscount findByProductDetail_Id(Long productDetailId);
    @Query(value = "SELECT * FROM product_discount pd " +
            "WHERE pd.product_detail_id = :productDetailId " +
            "AND GETDATE() BETWEEN pd.start_date AND pd.end_date " +
            "AND pd.closed = 'false'", nativeQuery = true)
    ProductDiscount findValidDiscountByProductDetailId(@Param("productDetailId") Long productDetailId);
}
