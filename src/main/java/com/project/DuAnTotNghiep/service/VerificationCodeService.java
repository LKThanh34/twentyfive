package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.VerificationCode;

import javax.mail.MessagingException;

public interface VerificationCodeService {
    VerificationCode createVerificationCode(String email) throws MessagingException;

    Account verifyCode( String code);
}