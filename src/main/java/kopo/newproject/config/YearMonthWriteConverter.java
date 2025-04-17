package kopo.newproject.config;

import org.springframework.core.convert.converter.Converter;
import java.time.YearMonth;

public class YearMonthWriteConverter implements Converter<YearMonth, String> {
    @Override
    public String convert(YearMonth source) {
        return source.toString();
    }
}
