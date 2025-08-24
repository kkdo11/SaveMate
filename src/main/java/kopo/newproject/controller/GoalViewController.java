package kopo.newproject.controller;

import kopo.newproject.dto.GoalDTO;
import kopo.newproject.service.impl.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

/**
 * 재정 목표(Goal) 관리 기능과 관련된 웹 페이지(View) 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @Controller} - 이 클래스가 Spring MVC의 컨트롤러임을 나타냅니다. 뷰의 이름을 반환하여 렌더링합니다.
 * {@code @RequestMapping("/goal")} - 이 컨트롤러의 모든 메소드는 '/goal' 경로 하위에 매핑됩니다.
 * {@code @RequiredArgsConstructor} - Lombok 어노테이션.
 */
@Slf4j
@Controller
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalViewController {

    private final GoalService goalService;

    /**
     * 재정 목표 관리 페이지를 사용자에게 보여줍니다.
     * <p>
     * 사용자가 로그인한 상태이면, 해당 사용자의 모든 목표 데이터를 조회하여 모델에 담아 뷰로 전달합니다.
     * 이를 통해 뷰에서는 목표 목록을 동적으로 표시할 수 있습니다.
     *
     * @param model 컨트롤러에서 뷰로 데이터를 전달하기 위한 객체
     * @return 렌더링할 뷰의 논리적인 이름. "goal/goalPage"는 'templates/goal/goalPage.html' 파일을 가리킵니다.
     */
    @GetMapping("/page")
    public String goalPage(Model model) {
        log.info("▶▶▶ [View] 재정 목표 페이지 요청");

        // Spring Security 컨텍스트에서 현재 사용자의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자가 인증되었는지, 그리고 익명 사용자가 아닌지 확인
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));

        String username = isAuthenticated ? authentication.getName() : "게스트";
        List<GoalDTO> goals = Collections.emptyList(); // 기본값으로 빈 리스트 초기화

        if (isAuthenticated) {
            log.info("인증된 사용자, 목표 목록을 조회합니다. | userId: {}", username);
            goals = goalService.getGoalsByUser(username);
            log.info("목표 {}건 조회 완료", goals.size());
        } else {
            log.info("인증되지 않은 사용자, 빈 목표 목록을 반환합니다.");
        }

        // 뷰에서 사용할 수 있도록 모델에 데이터 추가
        model.addAttribute("goals", goals);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", username);
        log.info("모델 데이터 추가 | username: {}, isAuthenticated: {}, goals count: {}", username, isAuthenticated, goals.size());

        log.info("◀◀◀ [View] 재정 목표 페이지 반환: templates/goal/goalPage.html");
        return "goal/goalPage";
    }
}
