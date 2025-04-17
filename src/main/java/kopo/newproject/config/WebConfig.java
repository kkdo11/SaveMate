/*
package kopo.newproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.YearMonth;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToYearMonthConverter());
    }

    static class StringToYearMonthConverter implements Converter<String, YearMonth> {
        @Override
        public YearMonth convert(String source) {
            return YearMonth.parse(source); // 또는 DateTimeFormatter.ofPattern("yyyy-MM")
        }
    }

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 요청에 대해 인터셉터를 적용 (로그인 상태 확인)
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 대해 적용
                .excludePathPatterns("/login", "/register");  // 로그인 및 회원가입 페이지는 제외
    }
}
*/
