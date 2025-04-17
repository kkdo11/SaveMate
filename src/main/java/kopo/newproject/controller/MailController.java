package kopo.newproject.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import kopo.newproject.dto.MailDTO;
import kopo.newproject.service.IMailService;
import kopo.newproject.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequestMapping("/mail")
public class MailController {

    @Resource(name = "MailService")
    private IMailService mailService;

    // 인증 코드 저장소 (일반적으로 Redis 사용 권장)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 메일 발송 폼
    @GetMapping("/mailForm")
    public String mailForm() {
        return "/mail/mailForm";
    }

    // 일반 메일 발송
    @PostMapping("/sendMail")
    public String sendMail(HttpServletRequest request, ModelMap model) {

        log.info("sendMail Start - {}", this.getClass().getName());

        String toMail = CmmUtil.nvl(request.getParameter("toMail"));
        String title = CmmUtil.nvl(request.getParameter("title"));
        String content = CmmUtil.nvl(request.getParameter("content"));

        MailDTO mailDTO = new MailDTO(toMail, title, content);

        int res = mailService.doSendMail(mailDTO);

        if (res == 1) {
            log.info("sendMail Success - {}", this.getClass().getName());
        } else {
            log.info("sendMail Fail - {}", this.getClass().getName());
        }

        model.addAttribute("res", String.valueOf(res));

        log.info("sendMail End - {}", this.getClass().getName());

        return "/mail/sendMailResult";
    }

    // 이메일 인증 코드 발송
    @PostMapping("/sendVerification")
    @ResponseBody
    public Map<String, String> sendVerificationCode(@RequestParam String email) {
        log.info("이메일 인증 요청 - {}", email);

        // 6자리 랜덤 인증 코드 생성
        String verificationCode = String.format("%06d", new Random().nextInt(1000000));

        // 이메일 발송
        MailDTO mailDTO = new MailDTO(email, "회원가입 인증 코드", "인증 코드: " + verificationCode);
        int result = mailService.doSendMail(mailDTO);

        // 인증 코드 저장
        verificationCodes.put(email, verificationCode);

        Map<String, String> response = new HashMap<>();
        if (result == 1) {
            response.put("message", "인증 코드가 이메일로 전송되었습니다.");
        } else {
            response.put("message", "이메일 전송 실패");
        }

        return response;
    }

    // 이메일 인증 코드 확인
    @PostMapping("/verifyCode")
    @ResponseBody
    public Map<String, String> verifyCode(@RequestParam String email, @RequestParam String code) {
        log.info("이메일 인증 코드 검증 요청 - 이메일: {}, 입력 코드: {}", email, code);

        Map<String, String> response = new HashMap<>();
        if (verificationCodes.containsKey(email) && verificationCodes.get(email).equals(code)) {
            response.put("message", "인증 성공");
            verificationCodes.remove(email); // 인증 후 삭제
        } else {
            response.put("message", "인증 실패");
        }

        return response;
    }
}
