package com.project.DuAnTotNghiep.service.serviceImpl;



import com.project.DuAnTotNghiep.dto.MailInfo;
import com.project.DuAnTotNghiep.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MailerServiceImpl implements MailerService {

    @Autowired
    private JavaMailSender sender;

    @Override
    public void send(MailInfo mail) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(mail.getFrom());
        helper.setTo(mail.getTo());
        helper.setSubject(mail.getSubject());
        helper.setText(mail.getBody(), true);
        helper.setReplyTo(mail.getFrom());
        String[] cc = mail.getCc();
        if (cc != null && cc.length > 0) {
            helper.setCc(cc);
        }
        String[] bcc = mail.getBcc();
        if (bcc != null && bcc.length > 0) {
            helper.setBcc(bcc);
        }
        String[] attachments = mail.getAttachments();
        if (attachments != null && attachments.length > 0) {
            for (String path : attachments) {
                File file = new File(path);
                helper.addAttachment(file.getName(), file);
            }
        }
        sender.send(message);
    }

    List<MailInfo> queue =new ArrayList<>();

    @Override
    public void queue(MailInfo mail) {
        queue.add(mail);
        System.out.println(queue.get(1));
    }


    @Scheduled(fixedDelay = 30000,initialDelay = 5000)
    public void run(){
        Date now=new Date();
        System.out.println("check email in queue "+ now.toString());
        while (!queue.isEmpty()){
            MailInfo mailInfo=queue.remove(0);
            try {
                System.out.println("send mail at: "+now.toString());
                send(mailInfo);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}