package kopo.newproject.service;

import kopo.newproject.dto.MailDTO;

public interface IMailService {

    int doSendMail(MailDTO mailDTO);

    String generateVerificationCode();

    int sendVerificationMail(String toMail, String verificationCode);
}


