package kopo.newproject.controller;

import kopo.newproject.dto.GoalDTO;

import kopo.newproject.service.impl.GoalService;
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
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalViewController {

    private final GoalService goalService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 목표 페이지 요청
    @GetMapping("/page")
    public String goalPage(Model model) {
        log.info("[View] 목표 페이지 요청됨");

        String userId = getCurrentUserId();
        boolean isAuthenticated = !"anonymousUser".equals(userId);
        List<GoalDTO> goals = isAuthenticated ? goalService.getGoalsByUser(userId) : List.of();

        model.addAttribute("goals", goals);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", isAuthenticated ? userId : "게스트");

        return "/goal/goalPage";
    }
}
