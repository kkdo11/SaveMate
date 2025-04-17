package kopo.newproject.controller;

import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
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
        log.info("[View] 지출 페이지 요청됨");
        List<SpendingEntity> spendings = spendingService.getSpendings(getCurrentUserId(), null, null);
        model.addAttribute("spendings", spendings);
        return "/spending/spendPage"; // thymeleaf 템플릿 반환
    }
}
