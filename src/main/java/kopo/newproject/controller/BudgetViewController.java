package kopo.newproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetViewController {

    @GetMapping("/page")
    public String budgetPage(Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAuthenticated = !"anonymousUser".equals(userId);

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", isAuthenticated ? userId : "게스트");

        return "/budget/budgetPage"; // templates/budget/budgetPage.html
    }
}
