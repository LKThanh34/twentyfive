package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.VerificationCode;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import com.project.DuAnTotNghiep.repository.VerificationRepository;
import com.project.DuAnTotNghiep.service.EmailService;
import com.project.DuAnTotNghiep.service.VerificationCodeService;
import com.project.DuAnTotNghiep.utils.RandomUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.LocalDateTime;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    private final VerificationRepository verificationRepository;

    private final AccountRepository accountRepository;

    private final EmailService emailService;

    public VerificationCodeServiceImpl(VerificationRepository verificationRepository, AccountRepository accountRepository, EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
    }

    @Override
    public VerificationCode createVerificationCode(String email) throws MessagingException {
        // Tạo mã xác nhận ngẫu nhiên
        String verificationCodeValue = generateRandomCode();

        // Thiết lập thời gian hết hạn cho mã xác nhận
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        Account account = accountRepository.findByEmail(email);
        if(account == null) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy tài khoản có địa chỉ email của bạn");
        }

        // Tạo đối tượng VerificationCode và lưu vào cơ sở dữ liệu
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setAccount(account);
        verificationCode.setCode(verificationCodeValue);
        verificationCode.setExpiryTime(expiryTime);



        StringBuilder str = new StringBuilder();
        str.append("Mã xác nhận của bạn là: ");
        str.append("<b>");
        str.append(verificationCodeValue);
        str.append("</b>");

        String subject = "Xác nhận đặt lại mật khẩu";
        emailService.sendEmail(account.getEmail(), subject, str.toString());

        return verificationRepository.save(verificationCode);
    }

    @Override
    public Account verifyCode(String code) {
        // Tìm mã xác nhận trong cơ sở dữ liệu
        VerificationCode verificationCode = verificationRepository.findByCode(code);

        if (verificationCode != null && isValid(verificationCode)) {
            // Mã xác nhận hợp lệ, trả về người dùng liên kết
            return verificationCode.getAccount();
        }

        // Mã xác nhận không hợp lệ hoặc đã hết hạn
        return null;
    }

    private boolean isValid(VerificationCode verificationCode) {
        // Kiểm tra xem mã xác nhận có hợp lệ và chưa hết hạn không
        LocalDateTime now = LocalDateTime.now();
        return verificationCode.getExpiryTime().isAfter(now);
    }

    private String generateRandomCode() {
        // Logic để tạo mã xác nhận ngẫu nhiên (ví dụ: sử dụng thư viện RandomStringUtils)
        return RandomUtils.randomAlphanumeric(6);
    }
}
