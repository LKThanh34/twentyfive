package com.project.DuAnTotNghiep.dto.Refund;

import java.time.LocalDateTime;

public interface RefundDto {
    String getBillCode();
    String getBillId();
    String getBillReturnId();
    String getOrderId();
    String getBillReturnCode();
    String getCustomerName();
    LocalDateTime getCancelDate();
    Double getTotalAmount();
    LocalDateTime getReturnDate();
    Double getReturnMoney();
    String getPaymentName();
    Integer getStatusExchange();
}
