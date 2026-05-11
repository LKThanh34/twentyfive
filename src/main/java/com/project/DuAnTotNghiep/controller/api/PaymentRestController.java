package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.config.ConfigVNPay;
import com.project.DuAnTotNghiep.dto.Payment.PaymentDto;
import com.project.DuAnTotNghiep.dto.Payment.PaymentResultDto;
import com.project.DuAnTotNghiep.entity.Payment;
import com.project.DuAnTotNghiep.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    private final PaymentRepository paymentRepository;

    public PaymentRestController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping()
    public ResponseEntity<PaymentDto> createPayment(HttpServletRequest req) {

        try {
            System.out.println("\n========== VNPAY DEBUG START ==========");

            // 1. thông tin cơ bản
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String orderType = "billpayment";

            // amount phải nhân 100
            long amount = Long.parseLong(req.getParameter("amount")) * 100;
            System.out.println("amount = " + amount);

            String vnp_TxnRef = ConfigVNPay.getRandomNumber(8);
            System.out.println("vnp_TxnRef = " + vnp_TxnRef);

            String vnp_IpAddr = ConfigVNPay.getIpAddress(req);
            System.out.println("vnp_IpAddr = " + vnp_IpAddr);

            String vnp_TmnCode = ConfigVNPay.vnp_TmnCode;

            // 2. lưu payment vào DB trước
            PaymentResultDto paymentResultDto = new PaymentResultDto();
            paymentResultDto.setTxnRef(vnp_TxnRef);
            paymentResultDto.setAmount(String.valueOf(amount / 100));
            savePaymentToDB(paymentResultDto);

            // 3. tạo params gửi VNPay
            Map<String, String> vnp_Params = new TreeMap<>();

            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_BankCode", "NCB");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", ConfigVNPay.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            // 4. thời gian tạo
            Calendar cld = Calendar.getInstance(
                    TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            System.out.println("vnp_CreateDate = " + vnp_CreateDate);

            // hết hạn sau 15 phút
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            System.out.println("vnp_ExpireDate = " + vnp_ExpireDate);

            // 5. sort params
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());

            Collections.sort(fieldNames);

            // 6. build hashData và query
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            boolean first = true;

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (!first) {
                        hashData.append("&");
                        query.append("&");
                    }
                    first = false;

                    
                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                   
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                }
            }
            System.out.println("hashData = " + hashData);

            String queryUrl = query.toString();
            System.out.println("queryUrl = " + queryUrl);

            // 7. tạo chữ ký
            String vnp_SecureHash = ConfigVNPay.hmacSHA512(
                    ConfigVNPay.secretKey,
                    hashData.toString());

            System.out.println("secretKey.length = " + ConfigVNPay.secretKey.length()); // phải là 33
            System.out.println("secureHash = " + vnp_SecureHash);

            // 8. thêm chữ ký vào URL

            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            String paymentUrl = ConfigVNPay.vnp_PayUrl + "?" + queryUrl;

            System.out.println("paymentUrl = " + paymentUrl);
            System.out.println("========== VNPAY DEBUG END ==========\n");

            PaymentDto paymentDto = new PaymentDto(
                    "OK",
                    "success",
                    paymentUrl);

            return ResponseEntity.ok(paymentDto);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.badRequest().body(
                    new PaymentDto(
                            "ERROR",
                            e.getMessage(),
                            null));
        }
    }

    private void savePaymentToDB(
            PaymentResultDto paymentResultDto) {
        Payment payment = new Payment();

        payment.setOrderId(
                paymentResultDto.getTxnRef());

        payment.setAmount(
                paymentResultDto.getAmount());

        payment.setOrderStatus("0");
        payment.setStatusExchange(0);

        paymentRepository.save(payment);
    }

}