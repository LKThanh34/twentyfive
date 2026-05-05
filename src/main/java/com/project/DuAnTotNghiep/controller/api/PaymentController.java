package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.config.ConfigVNPay;
import com.project.DuAnTotNghiep.dto.Payment.PaymentResultDto;
import com.project.DuAnTotNghiep.entity.Payment;
import com.project.DuAnTotNghiep.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payment-result")
    public String viewPaymentResult(HttpServletRequest request, Model model) throws UnsupportedEncodingException {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = ConfigVNPay.hashAllFields(fields);
        PaymentResultDto paymentResultDto = new PaymentResultDto();
        paymentResultDto.setTxnRef(fields.get("vnp_TxnRef").toString());
        paymentResultDto.setAmount(String.valueOf(Double.parseDouble(fields.get("vnp_Amount").toString()) / 100));
        paymentResultDto.setBankCode(fields.get("vnp_BankCode").toString());
        paymentResultDto.setDatePay(fields.get("vnp_PayDate").toString());
        paymentResultDto.setResponseCode(fields.get("vnp_ResponseCode").toString());
        paymentResultDto.setTransactionStatus(fields.get("vnp_TransactionStatus").toString());

        model.addAttribute("result", paymentResultDto);
        if (signValue.equals(vnp_SecureHash)) {
            boolean checkOrderId = paymentRepository.existsByOrderId(paymentResultDto.getTxnRef());   // Giá trị của vnp_TxnRef tồn tại trong CSDL của merchant
            boolean checkAmount = false; // De kiem tra amount
            boolean checkOrderStatus = false; // De kiem tra status co phai la da thanh toan hay khong
            if (checkOrderId) {
                Payment payment = paymentRepository.findByOrderId(paymentResultDto.getTxnRef());
                checkAmount = Double.parseDouble(paymentResultDto.getAmount()) == Double.parseDouble(payment.getAmount());
                checkOrderStatus = payment.getOrderStatus().equals("0");
                if (checkAmount) {
                    if (checkOrderStatus) {
                        if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                            model.addAttribute("status", "Giao dịch thành công");
                            model.addAttribute("paymentSuccess", true);
                            model.addAttribute("orderId", paymentResultDto.getTxnRef());
                            updatePaymentStatus(payment);
                        } else {
                            model.addAttribute("status", "GD Không thành công");
                            model.addAttribute("paymentSuccess", false);
                        }
                    }
                    else {
                        model.addAttribute("status", "Đơn hàng đã được thanh toán");
                        model.addAttribute("paymentSuccess", false);

                    }
                } else {
                    model.addAttribute("status", "Số tiền không khớp");
                    model.addAttribute("paymentSuccess", false);

                }
            } else {
                model.addAttribute("status", "Mã giao dịch không tồn tại");
                model.addAttribute("paymentSuccess", false);

            }
        } else {
            model.addAttribute("status", "Invalid checksum");
            model.addAttribute("paymentSuccess", false);

        }
        return "user/payment-result";
    }

    private void updatePaymentStatus(Payment payment) {
        payment.setOrderStatus("1");
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}
