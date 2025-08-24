package kopo.newproject.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 컨트롤러에 CSRF(Cross-Site Request Forgery) 토큰을 자동으로 전달하기 위한 전역 어드바이스.
 * <p>
 * {@code @ControllerAdvice} 어노테이션을 통해, 이 클래스의 설정은 애플리케이션 내의 모든 {@code @Controller}에 적용됩니다.
 * 이를 통해 개발자가 각 컨트롤러 메소드마다 CSRF 토큰을 수동으로 모델에 추가하는 반복적인 작업을 생략할 수 있습니다.
 */
@Slf4j
@ControllerAdvice
public class GlobalCsrfAdvice {

    /**
     * 모든 뷰(HTML)에 렌더링될 모델에 CSRF 토큰 정보를 추가합니다.
     * <p>
     * {@code @ModelAttribute} 어노테이션 덕분에, 모든 컨트롤러의 요청 처리 전에 이 메소드가 먼저 실행됩니다.
     * Spring Security가 생성한 현재 요청의 {@link CsrfToken}을 파라미터로 주입받아,
     * 모델에 "_csrf"라는 이름으로 추가합니다.
     * <p>
     * 이렇게 추가된 토큰은 Thymeleaf 같은 템플릿 엔진에서 `_csrf.token`, `_csrf.headerName`, `_csrf.parameterName` 등으로 쉽게 접근하여
     * form의 hidden input이나 AJAX 요청의 헤더에 사용할 수 있습니다.
     *
     * @param model 컨트롤러에서 뷰로 데이터를 전달하는 데 사용되는 객체
     * @param token 현재 HTTP 요청에 대한 CSRF 토큰 정보
     */
    @ModelAttribute
    public void addCsrfToken(Model model, CsrfToken token) {
        if (token != null) {
            log.debug("Adding CSRF token to model for request. Token Parameter Name: {}, Header Name: {}", token.getParameterName(), token.getHeaderName());
            model.addAttribute("_csrf", token);
        } else {
            log.warn("CSRF token is null for this request. CSRF protection might be disabled for this path.");
        }
    }
}
