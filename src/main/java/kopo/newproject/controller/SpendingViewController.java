package kopo.newproject.controller;

import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/spending")
@RequiredArgsConstructor
public class SpendingViewController {

    private final ISpendingService spendingService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/page")
    public String spendingPage(Model model) {

        String userId = getCurrentUserId();
        boolean isAuthenticated = !"anonymousUser".equals(userId);

        List<SpendingEntity> spendings = isAuthenticated
                ? spendingService.getSpendings(userId, null, null)
                : List.of();

        model.addAttribute("spendings", spendings);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", isAuthenticated ? userId : "게스트");

        return "spending/spendPage";
    }
}
