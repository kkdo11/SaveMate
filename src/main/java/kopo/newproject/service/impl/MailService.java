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

/**
 * {@link IMailService} 인터페이스의 구현체.
 * <p>
 * Spring의 {@link JavaMailSender}를 사용하여 이메일 발송 기능을 제공합니다.
 * 일반 메일 발송 및 이메일 인증 코드 발송 로직을 포함합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service("MailService") // 서비스 빈 이름 명시
public class MailService implements IMailService {

    private final JavaMailSender mailSender; // 이메일 발송을 위한 Spring MailSender
    @Value("${spring.mail.username}")
    private String fromMail; // application.properties에서 주입받는 발신자 이메일 주소

    /**
     * {@inheritDoc}
     */
    @Override
    public int doSendMail(MailDTO mailDTO) {
        log.info("▶▶▶ [Service Start] doSendMail | 이메일 발송 요청");
        int res = 1; // 기본 성공 값

        if (mailDTO == null) {
            log.warn("MailDTO가 null입니다. 이메일 발송을 중단합니다.");
            return 0;
        }

        // MailDTO에서 필요한 정보 추출 및 null 방지 처리
        String toMail = CmmUtil.nvl(mailDTO.getToMail());
        String title = CmmUtil.nvl(mailDTO.getTitle());
        String contents = CmmUtil.nvl(mailDTO.getContents());
        log.debug("발송 정보 | To: {}, From: {}, Title: {}", toMail, fromMail, title);

        try {
            // MimeMessage 객체 생성: 복잡한 이메일(HTML, 첨부파일 등)을 보낼 때 사용
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // MimeMessageHelper를 사용하여 MimeMessage 설정 간소화
            // 두 번째 파라미터 true는 멀티파트 메시지(첨부파일 등)를 지원함을 의미
            // 세 번째 파라미터 "UTF-8"은 인코딩 설정
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setTo(toMail); // 수신자 설정
            mimeMessageHelper.setFrom(fromMail); // 발신자 설정 (application.properties에서 주입받은 값)
            mimeMessageHelper.setSubject(title); // 제목 설정
            // 메일 내용 설정. 두 번째 파라미터 true는 내용이 HTML 형식임을 의미
            mimeMessageHelper.setText(contents, true);

            mailSender.send(mimeMessage); // 이메일 발송
            log.info("이메일 발송 성공 | To: {}, Title: {}", toMail, title);

        } catch (Exception e) {
            log.error("이메일 발송 중 오류 발생 | To: {}, Title: {}", toMail, title, e);
            res = 0; // 오류 발생 시 결과 0으로 설정
        } finally {
            log.info("◀◀◀ [Service End] doSendMail | 이메일 발송 처리 완료 | 결과: {}", res);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateVerificationCode() {
        log.info("▶▶▶ [Service Start] generateVerificationCode | 인증 코드 생성 요청");
        Random random = new Random();
        // 100000 (포함) ~ 999999 (포함) 범위의 6자리 숫자 생성
        int code = 100000 + random.nextInt(900000);
        String generatedCode = String.valueOf(code);
        log.info("◀◀◀ [Service End] generateVerificationCode | 생성된 인증 코드: {}", generatedCode);
        return generatedCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sendVerificationMail(String toMail, String verificationCode) {
        log.info("▶▶▶ [Service Start] sendVerificationMail | 인증 메일 발송 요청 | To: {}", toMail);
        String title = "SAVEMATE 회원가입 이메일 인증";
        String contents = "SAVEMATE 회원가입 인증번호 : <b>" + CmmUtil.nvl(verificationCode) + "</b><br><br>" +
                          "이 코드를 회원가입 페이지에 입력하여 인증을 완료해주세요.";

        MailDTO mailDTO = MailDTO.builder()
                .toMail(toMail)
                .title(title)
                .contents(contents)
                .build();

        // 실제 메일 발송은 doSendMail 메소드에 위임
        int res = doSendMail(mailDTO);
        log.info("◀◀◀ [Service End] sendVerificationMail | 인증 메일 발송 처리 완료 | 결과: {}", res);
        return res;
    }
}
