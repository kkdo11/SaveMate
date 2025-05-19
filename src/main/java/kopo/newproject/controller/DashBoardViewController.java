package kopo.newproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashBoardViewController {

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }



    @GetMapping("/page")
    public String getDashboardPage(Model model) {
        String userId = getCurrentUserId();
        log.info("✅ 대시보드 페이지 요청됨: userId={}", userId);
        model.addAttribute("username", userId);
        return "dashboard/dashBoard"; // templates/dashboard/dashBoard.html
    }
}
