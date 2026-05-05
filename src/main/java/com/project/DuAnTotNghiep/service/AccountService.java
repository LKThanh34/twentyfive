package com.project.DuAnTotNghiep.service;


import com.project.DuAnTotNghiep.dto.Account.AccountDto;
import com.project.DuAnTotNghiep.dto.Account.ChangePasswordDto;
import com.project.DuAnTotNghiep.dto.Statistic.UserStatistic;
import com.project.DuAnTotNghiep.entity.Account;

import java.util.List;

public interface AccountService {
    Account findByEmail(String email);

    List<Account> findAllAccount();
    Account save(Account account);

    List<UserStatistic> getUserStatistics(String startDate, String endDate);

    Account blockAccount(Long id);

    Account openAccount(Long id);

    Account changeRole(String email, Long roleId);

    AccountDto getAccountLogin();

    AccountDto updateProfile(AccountDto accountDto);

    void changePassword(ChangePasswordDto changePasswordDto);

    void resetPassword(Account account, String newPassword);
}
