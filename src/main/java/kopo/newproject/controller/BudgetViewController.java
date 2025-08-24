package kopo.newproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 예산(Budget) 관리 기능과 관련된 웹 페이지(View) 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @Controller} - 이 클래스가 Spring MVC의 컨트롤러임을 나타냅니다. 뷰의 이름을 반환하여 렌더링합니다.
 * {@code @RequestMapping("/budget")} - 이 컨트롤러의 모든 메소드는 '/budget' 경로 하위에 매핑됩니다.
 * {@code @RequiredArgsConstructor} - Lombok 어노테이션.
 */
@Slf4j
@Controller
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetViewController {

    /**
     * 예산 관리 페이지를 사용자에게 보여줍니다.
     * <p>
     * 현재 사용자의 로그인 상태를 확인하여, 관련 정보를 모델에 담아 뷰로 전달합니다.
     *
     * @param model 컨트롤러에서 뷰로 데이터를 전달하기 위한 객체
     * @return 렌더링할 뷰의 논리적인 이름. "budget/budgetPage"는 'templates/budget/budgetPage.html' 파일을 가리킵니다.
     */
    @GetMapping("/page")
    public String budgetPage(Model model) {
        log.info("▶▶▶ [View] 예산 관리 페이지 요청");

        // Spring Security 컨텍스트에서 현재 사용자의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자가 인증되었는지, 그리고 익명 사용자가 아닌지 확인
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));

        String username = isAuthenticated ? authentication.getName() : "게스트";

        // 뷰에서 사용할 수 있도록 모델에 데이터 추가
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", username);
        log.info("모델 데이터 추가 | username: {}, isAuthenticated: {}", username, isAuthenticated);

        log.info("◀◀◀ [View] 예산 관리 페이지 반환: templates/budget/budgetPage.html");
        return "budget/budgetPage";
    }
}
