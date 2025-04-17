package kopo.newproject.service.impl;

import jakarta.mail.internet.MimeMessage;
import kopo.newproject.dto.MailDTO;
import kopo.newproject.service.IMailService;
import kopo.newproject.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service("MailService")
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public int doSendMail(MailDTO mailDTO) {
        log.info("doSendMail start", this.getClass().getName());

        int res = 1;
        if (mailDTO == null) {
            mailDTO = new MailDTO();
        }

        String toMail = CmmUtil.nvl(mailDTO.getToMail());
        String title = CmmUtil.nvl(mailDTO.getTitle());
        String contents = CmmUtil.nvl(mailDTO.getContents());

        log.info("toMail:{}", toMail);
        log.info("title:{}", title);
        log.info("contents:{}", contents);
        log.info("Sending mail : {}", mailDTO);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            mimeMessageHelper.setTo(toMail);
            mimeMessageHelper.setFrom(fromMail);
            mimeMessageHelper.setSubject(title);
            // HTML 형식의 메일 발송 (두 번째 파라미터 true)
            mimeMessageHelper.setText(contents, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            res = 0;
            log.error("doSendMail Exception in {}: {}", this.getClass().getName(), e.getMessage(), e);
        }

        log.info("doSendMail end", this.getClass().getName());
        return res;
    }

    /**
     * 인증 메일 발송 메서드
     * @param toMail 받는 사람 이메일
     * @param verificationCode 생성된 인증 코드
     * @return 발송 결과 (성공: 1, 실패: 0)
     */
    public int sendVerificationMail(String toMail, String verificationCode) {
        String title = "SAVEMATE 회원가입 이메일 인증";
        String contents = "SAVEMATE 회원가입 인증번호 : " + CmmUtil.nvl(verificationCode);

        MailDTO mailDTO = MailDTO.builder()
                .toMail(toMail)
                .title(title)
                .contents(contents)
                .build();
        return doSendMail(mailDTO);
    }

    /**
     * 6자리 인증 코드를 생성하는 유틸리티 메서드
     * @return 6자리 인증 코드 (문자열)
     */
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}