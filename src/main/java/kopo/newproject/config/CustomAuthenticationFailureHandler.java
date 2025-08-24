package kopo.newproject.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security의 로그인 실패 처리를 담당하는 커스텀 핸들러.
 * <p>
 * 사용자가 로그인에 실패했을 때, 기본 동작을 재정의하여 추가적인 로직을 수행합니다.
 * 여기서는 실패 시, 요청(request)에 특정 에러 메시지를 담아 로그인 페이지로 전달하는 역할을 합니다.
 * <p>
 * {@code @Component} - 이 클래스를 Spring Bean으로 등록하여, SecurityConfig에서 주입받아 사용할 수 있도록 합니다.
 */
@Slf4j
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    /**
     * 인증(로그인) 실패 시 Spring Security에 의해 자동으로 호출되는 메소드.
     *
     * @param request   클라이언트의 요청 정보
     * @param response  서버의 응답 정보
     * @param exception 발생한 인증 관련 예외 객체. 실패 원인에 대한 정보를 담고 있습니다.
     * @throws IOException      입출력 예외 발생 시
     * @throws ServletException 서블릿 관련 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 로그인 실패 원인을 로그로 기록합니다. 어떤 사용자가 어떤 이유로 실패했는지 추적하는데 중요합니다.
        String username = request.getParameter("user_id"); // 로그인 폼에서 사용하는 username 파라미터
        log.warn("로그인 실패: 사용자 = {}, 원인 = {}", username, exception.getMessage());

        // 프론트엔드(로그인 페이지)에 표시할 에러 메시지를 request attribute에 추가합니다.
        request.setAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");

        // 부모 클래스(SimpleUrlAuthenticationFailureHandler)의 기본 동작을 호출합니다.
        // 이 메소드는 설정된 defaultFailureUrl로 리다이렉트하는 역할을 수행합니다.
        super.onAuthenticationFailure(request, response, exception);
    }
}
