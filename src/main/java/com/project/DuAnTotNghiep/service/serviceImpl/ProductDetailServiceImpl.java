package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Product.ProductDetailDto;
import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import com.project.DuAnTotNghiep.entity.ProductDiscount;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.repository.ProductDetailRepository;
import com.project.DuAnTotNghiep.repository.ProductDiscountRepository;
import com.project.DuAnTotNghiep.repository.ProductRepository;
import com.project.DuAnTotNghiep.service.ProductDetailService;
import com.project.DuAnTotNghiep.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {
@Autowired
private ProductDetailRepository productDetailRepository;
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductDiscountRepository productDiscountRepository;

    @Override
    public ProductDetail save(ProductDetail productDetail) {
        if (productDetail.getId() == null){
            return productDetailRepository.save(productDetail);
        }else{
            ProductDetail productDetail1 = productDetailRepository.getOne(productDetail.getId());
            int i = productDetail.getQuantity();
            int b = productDetail1.getQuantity();
            productDetail.setQuantity(b - i);
            return productDetailRepository.save(productDetail);
        }

    }


    @Override
    public ProductDetail getProductDetailByProductCode(String code){
        Product product = productRepository.findByCode(code);

        return productDetailRepository.getProductDetailByProduct(product);

    }

    @Override
    public List<ProductDetailDto> getByProductId(Long id) throws NotFoundException {
        Product product = productRepository.findById(id).orElseThrow( () -> new NotFoundException("Product not found"));
        List<ProductDetail> productDetails = productDetailRepository.getProductDetailByProductId(id);
        List<ProductDetailDto> productDetailDTOs = new ArrayList<>();

        for (ProductDetail productDetail : productDetails) {
            ProductDetailDto productDetailDTO = new ProductDetailDto();
            // Set properties of productDetailDTO based on productDetail
            productDetailDTO.setId(productDetail.getId());
            productDetailDTO.setProductId(productDetail.getProduct().getId());
            productDetailDTO.setPrice(productDetail.getPrice());
            productDetailDTO.setSize(productDetail.getSize());
            productDetailDTO.setColor(productDetail.getColor());
            productDetailDTO.setQuantity(productDetail.getQuantity());

            ProductDiscount productDiscount = productDiscountRepository.findValidDiscountByProductDetailId(productDetail.getId());
            if(productDiscount != null) {
//                Date endDate = productDiscount.getEndDate();
//                Date currentDate = new Date();
//                if (currentDate.compareTo(endDate) > 0) {
//                }
                productDetailDTO.setDiscountedPrice(productDiscount.getDiscountedAmount());

            }
            // Set other properties as needed
            productDetailDTOs.add(productDetailDTO);
        }
        return productDetailDTOs;
    }

}
