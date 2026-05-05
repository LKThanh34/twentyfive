package com.project.DuAnTotNghiep.controller.user;

import com.project.DuAnTotNghiep.dto.Account.AccountDto;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.entity.Role;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import com.project.DuAnTotNghiep.repository.CustomerRepository;
import com.project.DuAnTotNghiep.service.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import java.time.LocalDateTime;

@Controller
public class AuthController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;

    private final CustomerRepository customerRepository;


    public AuthController(AccountService accountService, AccountRepository accountRepository, PasswordEncoder passwordEncoder, SessionService sessionService, CookieService cookieService, VerificationCodeService verificationCodeService, CustomerRepository customerRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationCodeService = verificationCodeService;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/user-login")
    public String viewLogin(Model model) {

        return "user/login";
    }

    @GetMapping("/forgot-pass")
    public String forgotPass(Model model){

        return "user/forgot-pass";
    }

    @GetMapping("/register")
    public String register(Model model,@ModelAttribute("Account") Account account){
        return "user/register";
    }

    @PostMapping("/register-save")
    public String saveregister(Model model, @Validated @ModelAttribute AccountDto accountDto, RedirectAttributes redirectAttributes) throws MessagingException {

        Account accountByEmail= accountService.findByEmail(accountDto.getEmail());

        //Kiểm tra xem số điện thoại đã có tài khoản chưa
        Account accountByPhone = accountRepository.findByCustomer_PhoneNumber(accountDto.getPhoneNumber());

        if(accountByEmail !=null ){
            redirectAttributes.addFlashAttribute("errorMessage","Email đã tồn tại !");
            return "redirect:/register";
        }
        if(accountByPhone != null) {
            redirectAttributes.addFlashAttribute("errorMessage","Số điện thoại " + accountDto.getPhoneNumber() + " đã được đăng ký!");
            return "redirect:/register";
        }

            Account account = new Account();
            account.setEmail(accountDto.getEmail());
            Account account1 = accountRepository.findTopByOrderByIdDesc();
            Long nextCode = (account1 == null) ? 1 : account1.getId() + 1;
            String accCode = "TK" + String.format("%04d", nextCode);
            account.setCode(accCode);

            String encoded = passwordEncoder.encode(accountDto.getPassword());
            account.setPassword(encoded);
            account.setNonLocked(true);
            Role role = new Role();
            role.setId(3L);
            account.setRole(role);
            Customer customer = null;

            //Nếu số điện thoại đã tồn tại
            if(customerRepository.existsByPhoneNumber(accountDto.getPhoneNumber())) {
                customer = customerRepository.findByPhoneNumber(accountDto.getPhoneNumber());
                customer.setName(accountDto.getName());
            }
            else {
                customer = new Customer();
                customer.setName(accountDto.getName());
                customer.setPhoneNumber(accountDto.getPhoneNumber());
                Customer customerCurrent = customerRepository.findTopByOrderByIdDesc();
                Long nextCodeAcc = (customerCurrent == null) ? 1 : customerCurrent.getId() + 1;
                String productCode = "KH" + String.format("%04d", nextCodeAcc);
                customer.setCode(productCode);

            }
            account.setCustomer(customer);
            account.setCreateDate(LocalDateTime.now());
            customerRepository.save(customer);
            accountService.save(account);
            redirectAttributes.addFlashAttribute("success", "Đăng ký tài khoản thành công");
            return "redirect:/user-login";
        }

        @PostMapping("/reset-page")
        public String viewResetPassPage(@RequestParam String email, RedirectAttributes redirectAttributes) throws MessagingException {
            try {
                verificationCodeService.createVerificationCode(email);
            }
            catch (ShopApiException exception) {
                redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                return "redirect:/forgot-pass";
            }
            return "redirect:/reset-pass";
        }

        @GetMapping("/reset-pass")
        public String viewResetPassPage() {
            return "user/reset-pass";
        }


    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String verificationCode,
                                @RequestParam String newPassword,
                                RedirectAttributes model) {
        // Kiểm tra mã xác nhận và lấy người dùng liên kết
        Account account = verificationCodeService.verifyCode(verificationCode);

        if (account != null) {
            // Đặt lại mật khẩu và xóa mã xác nhận
            accountService.resetPassword(account, newPassword);
            model.addFlashAttribute("success", "Đặt lại mật khẩu thành công");
            return "redirect:/user-login";
        } else {
            // Mã xác nhận không hợp lệ
            model.addFlashAttribute("errorMessage", "Mã xác thực không hợp lệ");
            return "redirect:/reset-pass";
        }
    }
}
