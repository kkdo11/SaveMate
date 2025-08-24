package kopo.newproject.config;

import kopo.newproject.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security의 핵심 설정을 담당하는 클래스.
 * <p>
 * {@code @Configuration} - 이 클래스가 Spring의 설정 정보를 담고 있음을 나타냅니다.
 * {@code @EnableWebSecurity} - 애플리케이션의 웹 보안을 활성화합니다. 이 어노테이션을 통해 Spring Security의 필터 체인이 적용됩니다.
 * {@code @RequiredArgsConstructor} - final 필드에 대한 생성자를 자동으로 생성해주는 Lombok 어노테이션입니다.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 사용자 정보를 DB에서 가져오는 서비스
    private final CustomUserDetailsService userDetailsService;
    // 로그인 실패 시 처리를 담당하는 커스텀 핸들러
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder를 Bean으로 등록하는 정적 내부 클래스.
     * SecurityConfig 클래스 자체의 순환 참조 문제를 방지하기 위해 정적 내부 클래스 패턴을 사용합니다.
     */
    @Configuration
    public static class PasswordEncoderConfig {
        /**
         * BCrypt 알고리즘을 사용하는 PasswordEncoder를 반환합니다.
         * BCrypt는 현재 가장 널리 사용되는 안전한 해시 알고리즘 중 하나입니다.
         * @return BCryptPasswordEncoder 인스턴스
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
            log.info("BCryptPasswordEncoder Bean을 생성하여 등록합니다.");
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * 실제 인증을 처리할 인증 제공자(AuthenticationProvider)를 설정하고 Bean으로 등록합니다.
     * <p>
     * DaoAuthenticationProvider는 DB를 통해 사용자 정보를 조회하여 인증을 수행하는 가장 일반적인 방식입니다.
     * @param passwordEncoder 사용자 비밀번호를 검증할 때 사용할 PasswordEncoder
     * @return 설정이 완료된 DaoAuthenticationProvider 객체
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        log.info("DaoAuthenticationProvider Bean을 생성하여 등록합니다.");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 사용자 정보를 조회할 때 사용할 UserDetailsService 설정
        authProvider.setUserDetailsService(userDetailsService);
        // 비밀번호를 비교할 때 사용할 PasswordEncoder 설정
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * HTTP 요청에 대한 보안 규칙을 정의하는 SecurityFilterChain을 Bean으로 등록합니다.
     * 이 메소드에서 애플리케이션의 보안에 관한 거의 모든 설정이 이루어집니다.
     *
     * @param http HttpSecurity 객체. 보안 설정을 구성하는 빌더 역할을 합니다.
     * @return 구성이 완료된 SecurityFilterChain 객체
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        log.info("SecurityFilterChain을 설정합니다...");

        // 인증 제공자를 http 객체에 등록
        http.authenticationProvider(authenticationProvider);

        // API 요청을 식별하기 위한 RequestMatcher 정의
        AntPathRequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");
        AntPathRequestMatcher testApiMatcher = new AntPathRequestMatcher("/test/**");

        http
                // 1. CSRF(Cross-Site Request Forgery) 보호 설정
                .csrf(csrf -> csrf
                        // CSRF 토큰을 HttpOnly가 아닌 쿠키에 저장하여, JavaScript에서도 토큰에 접근할 수 있도록 허용합니다.
                        // 이는 AJAX 요청 시 헤더에 CSRF 토큰을 담아 보내기 위해 필요합니다.
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                // 2. URL별 권한 접근 제어 설정
                .authorizeHttpRequests(auth -> auth
                        // '/css/**', '/js/**', '/images/**' 같은 정적 자원들은 인증 없이 누구나 접근 가능
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // '/api/**', '/test/**' 경로의 API는 반드시 인증(로그인)된 사용자만 접근 가능
                        .requestMatchers(apiMatcher, testApiMatcher).authenticated()

                        // 아래 명시된 페이지들은 인증 없이 누구나 접근 가능
                        .requestMatchers(
                                "/user/login", "/user/loginProc", "/user/userRegForm",
                                "/user/insertUserInfo", "/user/getUserIdExists",
                                "/user/findPwd", "/user/findUserId", "/user/resetPassword",
                                "/user/sendVerificationEmail", "/user/getEmailExists", "/user/verifyEmailCode",
                                // 주요 콘텐츠 페이지는 비로그인 상태에서도 볼 수 있도록 허용 (단, 내부 데이터 조회 API는 막힘)
                                "/dashboard/page", "/spending/page", "/budget/page", "/goal/page", "/ai/page"
                        ).permitAll()

                        // '/user/myPage', '/user/changePassword' 페이지는 반드시 인증된 사용자만 접근 가능
                        .requestMatchers("/user/myPage", "/user/changePassword").authenticated()

                        // 위에 명시된 경로 외의 모든 요청은 반드시 인증(로그인)을 거쳐야 함
                        .anyRequest().authenticated()
                )
                // 3. 폼 기반 로그인 설정
                .formLogin(login -> login
                        // 커스텀 로그인 페이지 URL
                        .loginPage("/user/login")
                        // 로그인 처리(authentication)를 수행할 URL
                        .loginProcessingUrl("/user/loginProc")
                        // 로그인 폼에서 사용자 아이디에 해당하는 파라미터 이름
                        .usernameParameter("user_id")
                        // 로그인 폼에서 사용자 비밀번호에 해당하는 파라미터 이름
                        .passwordParameter("password")
                        // 로그인 성공 시 이동할 기본 URL. true로 설정하면 항상 이 URL로 이동.
                        .defaultSuccessUrl("/dashboard/page", true)
                        // 로그인 실패 시 처리를 담당할 커스텀 핸들러 등록
                        .failureHandler(customAuthenticationFailureHandler)
                        // 로그인 페이지는 누구나 접근 가능해야 하므로 permitAll() 설정
                        .permitAll()
                )
                // 4. 로그아웃 설정
                .logout(logout -> logout
                        // 로그아웃을 처리할 URL
                        .logoutUrl("/user/logout")
                        // 로그아웃 성공 시 이동할 URL
                        .logoutSuccessUrl("/user/login")
                        // 로그아웃 시 HTTP 세션을 무효화
                        .invalidateHttpSession(true)
                        // 로그아웃 시 'JSESSIONID' 쿠키 삭제
                        .deleteCookies("JSESSIONID")
                )
                // 5. 세션 관리 설정
                .sessionManagement(session -> session
                        // 동시 접속 가능한 최대 세션 수를 1로 제한 (중복 로그인 방지)
                        .maximumSessions(1)
                        // true: 새로운 로그인이 차단됨, false: 기존 세션이 만료됨
                        .maxSessionsPreventsLogin(false)
                )
                // 6. 예외 처리 설정
                .exceptionHandling(exception -> exception
                        // 인증되지 않은 사용자가 '/test/**' API에 접근 시, 로그인 페이지 리다이렉트 대신 401 Unauthorized 상태 코드 반환
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                testApiMatcher
                        )
                        // 인증되지 않은 사용자가 '/api/**' API에 접근 시, 로그인 페이지 리다이렉트 대신 401 Unauthorized 상태 코드 반환
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiMatcher
                        )
                );
        return http.build();
    }
}
