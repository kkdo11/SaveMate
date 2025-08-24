package kopo.newproject.config;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * MongoDB의 {@link Decimal128} 타입을 Java의 {@link BigDecimal} 타입으로 변환하는 커스텀 컨버터.
 * <p>
 * 이 컨버터는 Spring Data MongoDB가 DB에서 데이터를 읽어(Read)와서 엔티티 객체로 매핑할 때,
 * Decimal128 타입의 필드를 만나면 자동으로 Java의 BigDecimal 타입으로 변환해줍니다.
 * 이는 {@link BigDecimalToDecimal128Converter}와 쌍을 이루어 정확한 숫자 타입의 양방향 변환을 완성합니다.
 * <p>
 * {@code @Component} - 이 클래스를 Spring Bean으로 등록하여, 애플리케이션 컨텍스트에 의해 관리되도록 합니다.
 * {@code @ReadingConverter} - Spring Data의 어노테이션으로, DB에서 데이터를 읽는 방향의 변환을 처리함을 명시합니다.
 */
@Slf4j
@Component
@ReadingConverter
public class Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {

    /**
     * 주어진 Decimal128 소스를 BigDecimal 타입으로 변환합니다.
     *
     * @param source 변환할 원본 {@link Decimal128} 객체. null일 수 있습니다.
     * @return 변환된 {@link BigDecimal} 객체. 소스가 null이면 null을 반환합니다.
     */
    @Override
    public BigDecimal convert(Decimal128 source) {
        if (source == null) {
            return null;
        }
        log.debug("Decimal128 -> BigDecimal 변환 수행 : 원본 = {}", source);
        return source.bigDecimalValue();
    }
}
