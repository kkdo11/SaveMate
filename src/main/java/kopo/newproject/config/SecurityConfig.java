package kopo.newproject.config;

import jakarta.servlet.http.HttpServletResponse;
import kopo.newproject.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    // 인증 제공자 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Security 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        // API 요청에 대한 RequestMatcher 정의
        AntPathRequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");

        return http
                // 1. CSRF 보호 설정
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                // 2. URL별 권한 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 정적 자원 (CSS, JS, 이미지 등) - 무조건 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // API 경로는 인증된 사용자만 접근 가능
                        .requestMatchers(apiMatcher).authenticated()

                        // 인증 없이 접근 가능한 페이지 (로그인, 회원가입, 대시보드, 소비내역, 예산, 저축, AI분석 등)
                        .requestMatchers(
                                "/user/login", "/user/loginProc", "/user/userRegForm",
                                "/user/insertUserInfo", "/user/getUserIdExists",
                                "/user/findPwd", "/user/findUserId", "/user/resetPassword",
                                "/user/sendVerificationEmail", "/user/getEmailExists", "/user/verifyEmailCode",
                                "/dashboard/page", "/spending/page", "/budget/page", "/goal/page", "/ai/page","api/**"
                        ).permitAll()

                        // 인증된 사용자만 접근 가능한 페이지 (마이페이지, 비밀번호 변경 등)
                        .requestMatchers("/user/myPage", "/user/changePassword").authenticated()

                        // 그 외 모든 요청은 인증 필요 (기본값)
                        .anyRequest().authenticated()
                )
                // 3. 로그인 설정
                .formLogin(login -> login
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/loginProc")
                        .usernameParameter("user_id")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard/page", true)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                // 4. 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                // 5. 세션 관리
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                // 6. 예외 처리 (가장 중요)
                .exceptionHandling(exception -> exception
                        // 인증되지 않은 사용자가 API에 접근 시, 로그인 페이지로 리다이렉트 대신 401 Unauthorized 상태 코드 반환
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiMatcher
                        )
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Configuration
    public static class PasswordEncoderConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
