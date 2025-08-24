package kopo.newproject.util;

import lombok.extern.slf4j.Slf4j; // Slf4j 임포트 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 컨텍스트에서 현재 인증된 사용자 정보를 가져오는 유틸리티 클래스.
 * <p>
 * 주로 현재 로그인된 사용자의 아이디(username)를 안전하게 조회하는 메소드를 제공합니다.
 */
@Slf4j // 로그를 사용하기 위해 Slf4j 어노테이션 추가
public class AuthUtils {

    /**
     * 현재 로그인된 사용자의 아이디(username)를 반환합니다.
     * <p>
     * Spring Security의 {@link SecurityContextHolder}를 통해 현재 스레드의 보안 컨텍스트에 접근하여
     * {@link Authentication} 객체로부터 사용자 정보를 추출합니다.
     *
     * @return 현재 로그인된 사용자의 아이디(userId). 인증되지 않았거나 'anonymousUser'인 경우 null을 반환합니다.
     */
    public static String getCurrentUsername() {
        log.info("▶▶▶ [Util] getCurrentUsername | 현재 사용자 아이디 조회 시작");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            log.warn("인증 정보(Authentication)가 null입니다. 로그인된 사용자가 없습니다.");
            return null;
        }

        // Principal이 UserDetails 타입인지 확인 (일반적으로 로그인된 사용자)
        if (auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            log.info("◀◀◀ [Util] getCurrentUsername | 현재 사용자 아이디: {}", username);
            return username;
        } else if (auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal())) {
            // 'anonymousUser'는 로그인되지 않은 사용자를 나타내는 Spring Security의 기본 Principal입니다.
            log.info("◀◀◀ [Util] getCurrentUsername | 현재 사용자는 익명 사용자입니다.");
            return null; // 익명 사용자는 실제 사용자 ID가 아니므로 null 반환
        } else {
            log.warn("알 수 없는 Principal 타입입니다: {}", auth.getPrincipal().getClass().getName());
            return null;
        }
    }
}