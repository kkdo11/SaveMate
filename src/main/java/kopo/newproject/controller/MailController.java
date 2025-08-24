package kopo.newproject.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import kopo.newproject.dto.MailDTO;
import kopo.newproject.service.IMailService;
import kopo.newproject.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 이메일 발송 관련 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @Controller} - 이 클래스가 Spring MVC의 컨트롤러임을 나타냅니다.
 * 주로 뷰(HTML)의 이름을 반환하여, 해당 뷰를 렌더링하도록 합니다.
 * {@code @RequestMapping("/mail")} - 이 컨트롤러의 모든 메소드는 '/mail' 경로 하위에 매핑됩니다.
 */
@Slf4j
@Controller
@RequestMapping("/mail")
public class MailController {

    /**
     * {@code @Resource} 어노테이션을 사용하여 이름(name)으로 Spring Bean을 주입받습니다.
     * "MailService"라는 이름으로 등록된 Bean을 찾아 mailService 필드에 주입합니다.
     * {@code @Autowired}와 유사한 기능을 수행합니다.
     */
    @Resource(name = "MailService")
    private IMailService mailService;

    /**
     * 이메일 발송 폼 페이지를 보여주는 메소드.
     *
     * @return 렌더링할 뷰의 이름. 'templates/mail/mailForm.html'을 가리킵니다.
     */
    @GetMapping("/mailForm")
    public String mailForm() {
        log.info("▶▶▶ [View] 이메일 발송 폼 페이지 요청");
        log.info("◀◀◀ [View] 이메일 발송 폼 페이지 반환: templates/mail/mailForm.html");
        return "/mail/mailForm";
    }

    /**
     * 폼에서 입력받은 내용을 바탕으로 이메일을 발송하는 메소드.
     *
     * @param request HTTP 요청 정보. 폼 파라미터를 읽기 위해 사용됩니다.
     * @param model   컨트롤러에서 뷰로 데이터를 전달하기 위한 객체
     * @return 렌더링할 결과 페이지 뷰의 이름. 'templates/mail/sendMailResult.html'을 가리킵니다.
     */
    @PostMapping("/sendMail")
    public String sendMail(HttpServletRequest request, ModelMap model) {
        log.info("▶▶▶ [Proc] 이메일 발송 처리 시작");

        // 1. 폼에서 전송된 파라미터 받기
        // CmmUtil.nvl은 파라미터 값이 null일 경우 빈 문자열로 안전하게 변환해주는 유틸리티입니다.
        String toMail = CmmUtil.nvl(request.getParameter("toMail"));
        String title = CmmUtil.nvl(request.getParameter("title"));
        String content = CmmUtil.nvl(request.getParameter("content"));
        log.info("수신자: {}, 제목: {}", toMail, title);

        // 2. MailDTO 객체 생성 및 서비스 호출
        MailDTO mailDTO = new MailDTO(toMail, title, content);
        int res = mailService.doSendMail(mailDTO);

        // 3. 발송 결과에 따른 처리
        if (res == 1) {
            log.info("이메일 발송 성공");
            // 성공 시 추가 로직이 필요하다면 여기에 작성
        } else {
            log.warn("이메일 발송 실패");
            // 실패 시 추가 로직이 필요하다면 여기에 작성
        }

        // 4. 뷰에 결과 전달
        model.addAttribute("res", String.valueOf(res));
        log.info("◀◀◀ [Proc] 이메일 발송 처리 완료 | 결과: {}", res);

        return "/mail/sendMailResult";
    }
}
