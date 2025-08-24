package kopo.newproject.config;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Java의 {@link BigDecimal} 타입을 MongoDB의 {@link Decimal128} 타입으로 변환하는 커스텀 컨버터.
 * <p>
 * MongoDB는 Java의 BigDecimal 타입을 직접 지원하지 않아, 정확한 숫자 표현을 위해 BSON의 Decimal128 타입 사용이 권장됩니다.
 * 이 컨버터는 Spring Data MongoDB가 엔티티를 DB에 저장(Write)할 때, BigDecimal 필드를 만나면 자동으로 Decimal128로 변환해줍니다.
 * <p>
 * {@code @Component} - 이 클래스를 Spring Bean으로 등록하여, 애플리케이션 컨텍스트에 의해 관리되도록 합니다.
 * {@code @WritingConverter} - Spring Data의 어노테이션으로, DB에 데이터를 쓰는 방향의 변환을 처리함을 명시합니다.
 */
@Slf4j
@Component
@WritingConverter
public class BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {

    /**
     * 주어진 BigDecimal 소스를 Decimal128 타입으로 변환합니다.
     *
     * @param source 변환할 원본 {@link BigDecimal} 객체. null일 수 있습니다.
     * @return 변환된 {@link Decimal128} 객체. 소스가 null이면 null을 반환합니다.
     */
    @Override
    public Decimal128 convert(BigDecimal source) {
        if (source == null) {
            return null;
        }
        log.debug("BigDecimal -> Decimal128 변환 수행 : 원본 = {}", source);
        return new Decimal128(source);
    }
}

