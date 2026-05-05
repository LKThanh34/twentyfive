package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.BillReturn.*;
import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.entity.*;
import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.*;
import com.project.DuAnTotNghiep.repository.Specification.BillReturnSpecification;
import com.project.DuAnTotNghiep.service.BillReturnService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillReturnServiceImpl implements BillReturnService {

    private final BillReturnRepository billReturnRepository;
    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final ProductDetailRepository  productDetailRepository;
    private final ReturnDetailRepository returnDetailRepository;

    public BillReturnServiceImpl(BillReturnRepository billReturnRepository, BillRepository billRepository, BillDetailRepository billDetailRepository, ProductDetailRepository productDetailRepository, ProductDiscountRepository productDiscountRepository, ProductRepository productRepository, CustomerRepository customerRepository, ProductDetailRepository productDetailRepository1, ReturnDetailRepository returnDetailRepository) {
        this.billReturnRepository = billReturnRepository;
        this.billRepository = billRepository;
        this.billDetailRepository = billDetailRepository;
        this.productDiscountRepository = productDiscountRepository;
        this.productDetailRepository = productDetailRepository1;
        this.returnDetailRepository = returnDetailRepository;
    }

    @Override
    public List<BillReturnDto> getAllBillReturns(SearchBillReturnDto searchBillReturnDto) {
        Specification<BillReturn> spec = new BillReturnSpecification(searchBillReturnDto);
        List<BillReturn> billReturns = billReturnRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "returnDate"));
        List<BillReturnDto> billReturnDtos = billReturns.stream().map(this::convertToDto).collect(Collectors.toList());
        return billReturnDtos;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public BillReturnDto createBillReturn(BillReturnCreateDto billReturnCreateDto) {
        BillReturn billReturnLast = billReturnRepository.findTopByOrderByIdDesc();
        int nextCode = (billReturnLast == null) ? 1 : Integer.parseInt(billReturnLast.getCode().substring(2)) + 1;
        String billReturnCode = "DT" + String.format("%03d", nextCode);

        BillReturn billReturn = new BillReturn();
        billReturn.setCode(billReturnCode);
        billReturn.setReturnReason(billReturnCreateDto.getReason());
        billReturn.setReturnDate(LocalDateTime.now());
        billReturn.setCancel(false);

        Bill bill = billRepository.findById(billReturnCreateDto.getBillId()).orElseThrow(() -> new NotFoundException("Bill not found"));
        if(bill.getStatus() == BillStatus.TRA_HANG) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Đơn hàng đã được đổi trả");
        }
        bill.setReturnStatus(true);
        bill.setStatus(BillStatus.TRA_HANG);
        billRepository.save(bill);

        billReturn.setBill(bill);

           Double totalRefund = Double.valueOf(0);
           boolean checkQuantity = false;

           if(billReturnCreateDto.getRefundDtos() == null || billReturnCreateDto.getRefundDtos().isEmpty()) {
               throw new ShopApiException(HttpStatus.BAD_REQUEST, "Không có đơn trả nào");
           }
           for (RefundDto refundDto: billReturnCreateDto.getRefundDtos()) {
               if(refundDto.getQuantityRefund() > 0) {
                   checkQuantity = true;
               }

               BillDetail billDetail = billDetailRepository.findById(refundDto.getBillDetailId()).orElseThrow(() -> new NotFoundException("Không tìm thấy chi tiết hóa đơn"));

               //Nếu cần cộng lại vào sản phẩm cũ
               ProductDetail productDetailInBill = billDetail.getProductDetail();
               ProductDetail productDetailBefore = productDetailRepository.findById(productDetailInBill.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm " + productDetailInBill.getId()));
               int quantityBefore = productDetailBefore.getQuantity();
               productDetailBefore.setQuantity(quantityBefore + refundDto.getQuantityRefund());

               billDetail.setReturnQuantity(refundDto.getQuantityRefund());

               totalRefund += refundDto.getQuantityRefund() * billDetail.getMomentPrice();

               billDetailRepository.save(billDetail);
           }

           // Tính tiền hàng trả
           billReturn.setPercentFeeExchange(billReturnCreateDto.getPercent());
           Double refundToCustomer = totalRefund - totalRefund * billReturnCreateDto.getPercent() / 100;
           if(billReturnCreateDto.getReturnDtos() == null || billReturnCreateDto.getReturnDtos().isEmpty()) {
               billReturn.setReturnMoney(refundToCustomer);
               billReturn.setReturnStatus(3);
           }
           else {
               if(!billReturnCreateDto.getReturnDtos().isEmpty()) {
                   List<ReturnDetail> returnDetails = new ArrayList<>();
//                   if(billReturnCreateDto.getShipping()) {
//                       billReturn.setReturnStatus(0);
//                   }else {
                       billReturn.setReturnStatus(3);
//                   }
                   Double totalExchange = Double.valueOf(0);
                   for (ReturnDto returnDto:
                           billReturnCreateDto.getReturnDtos()) {
                       ReturnDetail returnDetail = new ReturnDetail();
                       ProductDetail productDetail = productDetailRepository.findById(returnDto.getProductDetailId()).orElseThrow(() -> new NotFoundException("Không tìm thấy chi tiết sản phẩm " + returnDto.getProductDetailId()));

                       //Giảm số lượng tồn
                       int oldQuantity = productDetail.getQuantity();
                       if(oldQuantity - returnDto.getQuantityReturn() < 0) {
                           throw new ShopApiException(HttpStatus.BAD_REQUEST, "Sản phẩm " + productDetail.getProduct().getName() + "-" + productDetail.getSize().getName() +  "-" + productDetail.getColor().getName()  + " chỉ còn lại " + productDetail.getQuantity());
                       }

                       productDetail.setQuantity(oldQuantity - returnDto.getQuantityReturn());

                       productDetailRepository.save(productDetail);

                       returnDetail.setBillReturn(billReturn);
                       returnDetail.setQuantityReturn(returnDto.getQuantityReturn());
                       returnDetail.setProductDetail(productDetail);

                       // Tính tiền sản phẩm hàng đổi
                       totalExchange += returnDto.getQuantityReturn() * productDetail.getPrice();
                       returnDetails.add(returnDetail);
                   }

                   // Tính tiền trả khách
                   if(totalRefund >= totalExchange) {
                       billReturn.setReturnMoney(totalExchange - totalRefund);
                   }
                   else {
                       billReturn.setReturnMoney(Double.valueOf(0));
                   }

                   returnDetailRepository.saveAll(returnDetails);
               }
           }


           billReturnRepository.save(billReturn);

        return convertToDto(billReturn);
    }

    @Override
    public BillReturnDetailDto getBillReturnDetailById(Long id) {
        BillReturn billReturn = billReturnRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn trả lại id: " + id));
        Bill bill = billReturn.getBill();
        BillReturnDetailDto billReturnDetailDto = new BillReturnDetailDto();
        billReturnDetailDto.setBillId(bill.getId());
        billReturnDetailDto.setBillCode(bill.getCode());
        billReturnDetailDto.setReturnDate(billReturn.getReturnDate());

        if(bill.getCustomer() != null) {
            billReturnDetailDto.setCustomerDto(new CustomerDto(bill.getCustomer().getId(), bill.getCustomer().getCode(), bill.getCustomer().getName(), bill.getCustomer().getPhoneNumber(), null, null));
        }

        List<BillDetail> billDetails = bill.getBillDetail();
        List<RefundProductDto> refundProductDtos = new ArrayList<>();

        // Lấy hàng trả
        for (BillDetail billDetail:
             billDetails) {
           if(billDetail.getReturnQuantity() != null) {
               if(billDetail.getReturnQuantity() > 0 ) {
                   RefundProductDto refundProductDto = new RefundProductDto();
                   ProductDetail productDetail = billDetail.getProductDetail();
                   refundProductDto.setProductName(productDetail.getProduct().getName());
                   refundProductDto.setMomentPriceRefund(billDetail.getMomentPrice());
                   refundProductDto.setProductDetailId(productDetail.getId());
                   refundProductDto.setColor(productDetail.getColor().getName());
                   refundProductDto.setSize(productDetail.getSize().getName());
                   refundProductDto.setQuantityRefund(billDetail.getReturnQuantity());
                   refundProductDtos.add(refundProductDto);
               }
           }
        }

        List<ReturnProductDto> returnProductDtos = new ArrayList<>();
        // Lấy hàng đổi
        for(ReturnDetail returnDetail: billReturn.getReturnDetails()) {
            ReturnProductDto returnProductDto = new ReturnProductDto();
            returnProductDto.setProductDetailId(returnDetail.getProductDetail().getId());
            returnProductDto.setQuantityReturn(returnDetail.getQuantityReturn());
            returnProductDto.setMomentPriceExchange(returnDetail.getProductDetail().getPrice());
            returnProductDto.setProductName(returnDetail.getProductDetail().getProduct().getName());
            returnProductDto.setSize(returnDetail.getProductDetail().getSize().getName());
            returnProductDto.setColor(returnDetail.getProductDetail().getColor().getName());
            returnProductDtos.add(returnProductDto);
        }
        billReturnDetailDto.setReturnProductDtos(returnProductDtos);

        billReturnDetailDto.setRefundProductDtos(refundProductDtos);
        billReturnDetailDto.setBillReturnCode(billReturn.getCode());

        billReturnDetailDto.setId(billReturn.getId());
        billReturnDetailDto.setReturnMoney(billReturn.getReturnMoney());
        billReturnDetailDto.setPercentFeeExchange(billReturn.getPercentFeeExchange());
        billReturnDetailDto.setBillReturnStatus(billReturn.getReturnStatus());
        return billReturnDetailDto;
    }

    @Override
    public BillReturnDetailDto getBillReturnDetailByCode(String code) {
        BillReturn billReturn = billReturnRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn trả lại id: " + code));
        Bill bill = billReturn.getBill();
        BillReturnDetailDto billReturnDetailDto = new BillReturnDetailDto();
        billReturnDetailDto.setBillId(bill.getId());
        billReturnDetailDto.setBillCode(bill.getCode());
        billReturnDetailDto.setReturnDate(billReturn.getReturnDate());

        if(bill.getCustomer() != null) {
            billReturnDetailDto.setCustomerDto(new CustomerDto(bill.getCustomer().getId(), bill.getCustomer().getCode(), bill.getCustomer().getName(), bill.getCustomer().getPhoneNumber(), null, null));
        }

        List<BillDetail> billDetails = bill.getBillDetail();
        List<RefundProductDto> refundProductDtos = new ArrayList<>();

        // Lấy hàng trả
        for (BillDetail billDetail:
                billDetails) {
            if(billDetail.getReturnQuantity() != null) {
                if(billDetail.getReturnQuantity() > 0 ) {
                    RefundProductDto refundProductDto = new RefundProductDto();
                    ProductDetail productDetail = billDetail.getProductDetail();
                    refundProductDto.setProductName(productDetail.getProduct().getName());
                    refundProductDto.setMomentPriceRefund(billDetail.getMomentPrice());
                    refundProductDto.setProductDetailId(productDetail.getId());
                    refundProductDto.setColor(productDetail.getColor().getName());
                    refundProductDto.setSize(productDetail.getSize().getName());
                    refundProductDto.setQuantityRefund(billDetail.getReturnQuantity());
                    refundProductDtos.add(refundProductDto);
                }
            }
        }

        List<ReturnProductDto> returnProductDtos = new ArrayList<>();
        // Lấy hàng đổi
        for(ReturnDetail returnDetail: billReturn.getReturnDetails()) {
            ReturnProductDto returnProductDto = new ReturnProductDto();
            returnProductDto.setProductDetailId(returnDetail.getProductDetail().getId());
            returnProductDto.setQuantityReturn(returnDetail.getQuantityReturn());
            returnProductDto.setMomentPriceExchange(returnDetail.getProductDetail().getPrice());
            returnProductDto.setProductName(returnDetail.getProductDetail().getProduct().getName());
            returnProductDto.setSize(returnDetail.getProductDetail().getSize().getName());
            returnProductDto.setColor(returnDetail.getProductDetail().getColor().getName());
            returnProductDtos.add(returnProductDto);
        }
        billReturnDetailDto.setReturnProductDtos(returnProductDtos);

        billReturnDetailDto.setRefundProductDtos(refundProductDtos);
        billReturnDetailDto.setBillReturnCode(billReturn.getCode());

        billReturnDetailDto.setId(billReturn.getId());
        billReturnDetailDto.setReturnMoney(billReturn.getReturnMoney());
        billReturnDetailDto.setPercentFeeExchange(billReturn.getPercentFeeExchange());
        billReturnDetailDto.setBillReturnStatus(billReturn.getReturnStatus());
        return billReturnDetailDto;
    }

    @Override
    public String generateHtmlContent(Long billReturnId) {
        BillReturnDetailDto billReturnDetailDto = getBillReturnDetailById(billReturnId);
        return null;
    }

    @Override
    public BillReturnDto updateStatus(Long id, int returnStatus) {
        BillReturn billReturn = billReturnRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn trả lại id: " + id));
        billReturn.setReturnStatus(returnStatus);
        if(returnStatus == 4) {
            for (ReturnDetail returnDetail:
                    billReturn.getReturnDetails()) {
                ProductDetail productDetail = returnDetail.getProductDetail();
                int quantityReturn = returnDetail.getQuantityReturn();
                int beforeQuantity = productDetail.getQuantity();
                productDetail.setQuantity(beforeQuantity + quantityReturn);
                productDetailRepository.save(productDetail);
            }
        }
        return convertToDto(billReturnRepository.save(billReturn));
    }

    private BillReturnDto convertToDto(BillReturn billReturn) {
        BillReturnDto billReturnDto = new BillReturnDto();
        billReturnDto.setId(billReturn.getId());
        billReturnDto.setCode(billReturn.getCode());
        billReturnDto.setReturnDate(billReturn.getReturnDate());
        billReturnDto.setReturnReason(billReturn.getReturnReason());
        billReturnDto.setCancel(billReturn.isCancel());
        billReturnDto.setReturnMoney(billReturn.getReturnMoney());
        billReturnDto.setReturnStatus(billReturn.getReturnStatus());
        if(billReturn.getBill().getCustomer() != null) {
            Customer customer = billReturn.getBill().getCustomer();
            CustomerDto customerDto = new CustomerDto(customer.getId(), customer.getCode(), customer.getName(), customer.getPhoneNumber(), customer.getEmail(), null);
            billReturnDto.setCustomer(customerDto);
        }
        return billReturnDto;
    }

}
