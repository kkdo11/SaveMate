package kopo.newproject.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Data MongoDB의 커스텀 설정을 담당하는 클래스.
 * <p>
 * {@code @Configuration} - 이 클래스가 Spring의 설정 정보를 담고 있음을 나타냅니다.
 */
@Slf4j
@Configuration
public class MongoConfig {

    /**
     * Spring Data MongoDB에서 사용할 커스텀 타입 변환기들을 등록합니다.
     * <p>
     * Java의 특정 타입(예: BigDecimal, YearMonth)을 MongoDB가 지원하는 BSON 타입으로,
     * 또는 그 반대로 변환하는 로직을 여기에 등록하여 자동으로 처리되도록 합니다.
     *
     * @return 커스텀 변환기 목록을 담고 있는 {@link MongoCustomConversions} 객체
     */
    @Bean
    public MongoCustomConversions customConversions() {
        log.info("MongoDB 커스텀 변환기들을 등록합니다...");
        List<Converter<?, ?>> converters = new ArrayList<>();

        // 직접 정의한 커스텀 컨버터들을 리스트에 추가
        converters.add(new YearMonthReadConverter());
        converters.add(new YearMonthWriteConverter());
        converters.add(new BigDecimalToDecimal128Converter());
        converters.add(new Decimal128ToBigDecimalConverter());

        log.info("{}개의 커스텀 변환기가 등록되었습니다.", converters.size());
        return new MongoCustomConversions(converters);
    }

    /**
     * DB에 저장된 문자열(예: "2025-08")을 Java의 {@link YearMonth} 객체로 변환하는 읽기 컨버터.
     */
    @Slf4j
    static class YearMonthReadConverter implements Converter<String, YearMonth> {
        @Override
        public YearMonth convert(String source) {
            if (source == null) {
                return null;
            }
            log.debug("String -> YearMonth 변환 수행 : 원본 = {}", source);
            return YearMonth.parse(source);
        }
    }

    /**
     * Java의 {@link YearMonth} 객체를 DB에 저장하기 위한 문자열(예: "2025-08")로 변환하는 쓰기 컨버터.
     */
    @Slf4j
    static class YearMonthWriteConverter implements Converter<YearMonth, String> {
        @Override
        public String convert(YearMonth source) {
            if (source == null) {
                return null;
            }
            log.debug("YearMonth -> String 변환 수행 : 원본 = {}", source);
            return source.toString();
        }
    }
}
