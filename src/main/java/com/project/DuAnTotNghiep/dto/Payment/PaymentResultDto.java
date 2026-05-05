package com.project.DuAnTotNghiep.dto.Payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResultDto {
    private String txnRef;
    private String amount;
    private String responseCode;
    private String bankCode;
    private String datePay;
    private String transactionStatus;
}
