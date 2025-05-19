package kopo.newproject.config;

import jakarta.servlet.http.HttpServletResponse;
import kopo.newproject.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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

        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // CSRF 활성화 및 쿠키 방식 설정
                )
                .authorizeHttpRequests(auth -> auth
                        // 로그인 관련 URL에 대해 접근을 허용
                        .requestMatchers("/user/insertUserInfo", "/user/findUserId", "/user/resetPassword",
                                "/user/findID", "/user/findPWD","user/findPwd", "/user/getUserIdExists",
                                "/user/sendVerificationEmail",
                                "/user/getEmailExists",
                                "/user/verifyEmailCode",
                                "/user/userRegForm", "/css/**", "/js/**", "/images/**",
                                "/user/login", "/user/loginsuccess","/spending/**").permitAll()

                        // 특정 경로인 /spending/page는 인증 없이 접근 허용
                        .requestMatchers("/spending/page","/budget/page","/goal/page","/ai/page").permitAll() // 인증 없이 접근 가능

                        // 지출 내역 관련 URL은 인증된 사용자만 접근 가능
                        .requestMatchers( "/spendingAPI/**","/budgetAPI/**","/dashboardAPI/**","/goalAPI/**","/api/analysis/**").authenticated() // /spendingAPI 경로 아래 모든 요청은 인증 필요

                        // 마이페이지와 패스워드 변경은 인증된 사용자만 접근
                        .requestMatchers("/user/mypage", "/user/changePassword").authenticated()

                        // 그 외의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/user/login") // 커스터마이징된 로그인 페이지
                        .loginProcessingUrl("/user/loginProc")
                        .usernameParameter("user_id")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard/page", true)
                        .failureHandler(customAuthenticationFailureHandler)
                        .failureUrl("/user/login?error=true")
                        .permitAll() // 로그인 페이지는 누구나 접근 가능
                )
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
//                        .logoutSuccessUrl("/user/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            } else {
                                response.sendRedirect("/user/login");
                            }
                        })
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
