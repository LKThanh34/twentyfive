package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Account.AccountDto;
import com.project.DuAnTotNghiep.dto.Account.ChangePasswordDto;
import com.project.DuAnTotNghiep.dto.AddressShipping.AddressShippingDto;
import com.project.DuAnTotNghiep.dto.Statistic.UserStatistic;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.AddressShipping;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.entity.Role;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import com.project.DuAnTotNghiep.repository.AddressShippingRepository;
import com.project.DuAnTotNghiep.repository.CustomerRepository;
import com.project.DuAnTotNghiep.service.AccountService;
import com.project.DuAnTotNghiep.utils.UserLoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressShippingRepository addressShippingRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public List<Account> findAllAccount() {
        return accountRepository.findAll();
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public List<UserStatistic> getUserStatistics(String startDate, String endDate) {
        List<UserStatistic> userStatistics = new ArrayList<>();

        List<Object[]> results = accountRepository.getMonthlyAccountStatistics(startDate, endDate);


        for (Object[] result : results) {
            String month = (String) result[0];
            Integer count = ((Number) result[1]).intValue();
            userStatistics.add(new UserStatistic(month, count));
        }

        return userStatistics;
    }

    @Override
    public Account blockAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(null);
        account.setNonLocked(false);
        return accountRepository.save(account);
    }

    @Override
    public Account openAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(null);
        account.setNonLocked(true);
        return accountRepository.save(account);
    }

    @Override
    public Account changeRole(String email, Long roleId) {
        Account account = accountRepository.findByEmail(email);
        Role role = new Role();
        role.setId(roleId);
        account.setRole(role);
        return accountRepository.save(account);
    }

    @Override
    public AccountDto getAccountLogin() {
        Account account = UserLoginUtil.getCurrentLogin();
        Customer customer = customerRepository.findByAccount_Id(account.getId());
        account.setCustomer(customer);
        return convertToDto(account);
    }

    @Override
    public AccountDto updateProfile(AccountDto accountDto) {
        Account account = UserLoginUtil.getCurrentLogin();
        Customer customer = customerRepository.findByAccount_Id(account.getId());
        if(!accountDto.getPhoneNumber().trim().equals(customer.getPhoneNumber())) {
            if(customerRepository.existsByPhoneNumber(accountDto.getPhoneNumber())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Số điện thoại " + accountDto.getPhoneNumber() + " đã được đăng ký");
            }
        }
        customer.setPhoneNumber(accountDto.getPhoneNumber());
        customer.setName(accountDto.getName());
        customerRepository.save(customer);
        return convertToDto(accountRepository.save(account));
    }

    @Override
    public void changePassword(ChangePasswordDto changePasswordDto) {
        Account account = UserLoginUtil.getCurrentLogin();
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), account.getPassword())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác");
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không khớp");
        }
        account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void resetPassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    private AccountDto convertToDto(Account account) {
        AccountDto accountDto = new AccountDto();
        accountDto.setEmail(account.getEmail());
        accountDto.setName(account.getCustomer().getName());
        accountDto.setPhoneNumber(account.getCustomer().getPhoneNumber());
        List<AddressShippingDto> addressShippingDtos = new ArrayList<>();
        List<AddressShipping> addressShippingList = addressShippingRepository.findAllByCustomer_Account_Id(account.getId());
        for (AddressShipping addressShipping:
             addressShippingList) {
            AddressShippingDto addressShippingDto = new AddressShippingDto();
            addressShippingDto.setId(addressShipping.getId());
            addressShippingDto.setAddress(addressShipping.getAddress());
            addressShippingDtos.add(addressShippingDto);
        }
        accountDto.setAddressShippingList(addressShippingDtos);
        return accountDto;
    }

    private Account convertToEntity(AccountDto accountDto) {
        Account account = new Account();
        account.setUpdateDate(LocalDateTime.now());
        account.setEmail(accountDto.getEmail());
        return account;
    }
}
