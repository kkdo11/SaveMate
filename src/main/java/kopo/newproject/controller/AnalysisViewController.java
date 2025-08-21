package kopo.newproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AnalysisViewController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisViewController.class);

    @GetMapping("/page")
    public String showAIAnalysisPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());

        String username = isAuthenticated ? authentication.getName() : "게스트";

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", username);

        log.info("🧠 [View] AI 분석 페이지 요청됨 - 사용자: {}, 로그인 상태: {}", username, isAuthenticated);

        return "ai/analysisPage"; // => templates/ai/analysisPage.html
    }
}
