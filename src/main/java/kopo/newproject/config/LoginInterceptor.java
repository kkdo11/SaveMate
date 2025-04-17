/*
package kopo.newproject.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 상태 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인된 사용자가 없으면 로그인 페이지로 리다이렉트
        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendRedirect("/login");
            return false;  // 요청을 중지하고 로그인 페이지로 리다이렉트
        }

        // 로그인된 사용자의 정보를 요청 속성에 저장 (템플릿에서 사용할 수 있도록)
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            request.setAttribute("username", userDetails.getUsername());  // 로그인한 사용자의 이름을 요청에 추가
        }

        return true;  // 요청을 계속 진행
    }
}
*/
