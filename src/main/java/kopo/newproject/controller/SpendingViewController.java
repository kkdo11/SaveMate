package kopo.newproject.controller;

import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.service.ISpendingService;
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
 * 지출(Spending) 내역 관리 기능과 관련된 웹 페이지(View) 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @Controller} - 이 클래스가 Spring MVC의 컨트롤러임을 나타냅니다. 뷰의 이름을 반환하여 렌더링합니다.
 * {@code @RequestMapping("/spending")} - 이 컨트롤러의 모든 메소드는 '/spending' 경로 하위에 매핑됩니다.
 * {@code @RequiredArgsConstructor} - Lombok 어노테이션.
 */
@Slf4j
@Controller
@RequestMapping("/spending")
@RequiredArgsConstructor
public class SpendingViewController {

    private final ISpendingService spendingService;

    /**
     * 지출 내역 관리 페이지를 사용자에게 보여줍니다.
     * <p>
     * 사용자가 로그인한 상태이면, 해당 사용자의 모든 지출 내역 데이터를 조회하여 모델에 담아 뷰로 전달합니다.
     *
     * @param model 컨트롤러에서 뷰로 데이터를 전달하기 위한 객체
     * @return 렌더링할 뷰의 논리적인 이름. "spending/spendPage"는 'templates/spending/spendPage.html' 파일을 가리킵니다.
     */
    @GetMapping("/page")
    public String spendingPage(Model model) {
        log.info("▶▶▶ [View] 지출 내역 페이지 요청");

        // Spring Security 컨텍스트에서 현재 사용자의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));

        String username = isAuthenticated ? authentication.getName() : "게스트";
        List<SpendingEntity> spendings = Collections.emptyList();

        if (isAuthenticated) {
            // TODO: [Performance] 사용자의 지출 내역이 매우 많아질 경우, 모든 내역을 한 번에 조회하면 성능 저하가 발생할 수 있습니다.
            //  향후 페이징(Paging) 기능을 도입하여 필요한 만큼만 조회하도록 개선하는 것을 고려해야 합니다.
            log.info("인증된 사용자, 지출 내역을 조회합니다. | userId: {}", username);
            spendings = spendingService.getSpendings(username, null, null);
            log.info("지출 내역 {}건 조회 완료", spendings.size());
        } else {
            log.info("인증되지 않은 사용자, 빈 지출 내역을 반환합니다.");
        }

        // 뷰에서 사용할 수 있도록 모델에 데이터 추가
        model.addAttribute("spendings", spendings);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", username);
        log.info("모델 데이터 추가 | username: {}, isAuthenticated: {}, spendings count: {}", username, isAuthenticated, spendings.size());

        log.info("◀◀◀ [View] 지출 내역 페이지 반환: templates/spending/spendPage.html");
        return "spending/spendPage";
    }
}
