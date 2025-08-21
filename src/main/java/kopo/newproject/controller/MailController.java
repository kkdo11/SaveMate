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

@Controller
@RequestMapping("/mail")
public class MailController {

    @Resource(name = "MailService")
    private IMailService mailService;

    // 메일 발송 폼
    @GetMapping("/mailForm")
    public String mailForm() {
        return "/mail/mailForm";
    }

    // 일반 메일 발송
    @PostMapping("/sendMail")
    public String sendMail(HttpServletRequest request, ModelMap model) {

        String toMail = CmmUtil.nvl(request.getParameter("toMail"));
        String title = CmmUtil.nvl(request.getParameter("title"));
        String content = CmmUtil.nvl(request.getParameter("content"));

        MailDTO mailDTO = new MailDTO(toMail, title, content);

        int res = mailService.doSendMail(mailDTO);

        if (res == 1) {
        } else {
        }

        model.addAttribute("res", String.valueOf(res));

        return "/mail/sendMailResult";
    }
}
