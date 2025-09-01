package kopo.newproject.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashBoardViewController {

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }



    @GetMapping("/page")
    public String getDashboardPage(Model model) {
        String userId = getCurrentUserId();
        model.addAttribute("username", userId);
        return "dashboard/dashBoard"; // templates/dashboard/dashBoard.html
    }
}
