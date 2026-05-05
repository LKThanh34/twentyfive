package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.MailInfo;

import javax.mail.MessagingException;

public interface MailerService {
    void send(MailInfo mail) throws MessagingException;

    void queue(MailInfo mail);
}
