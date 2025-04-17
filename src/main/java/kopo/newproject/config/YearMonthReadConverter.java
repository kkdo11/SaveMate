package kopo.newproject.config;

import org.springframework.core.convert.converter.Converter;
import java.time.YearMonth;

public class YearMonthReadConverter implements Converter<String, YearMonth> {
    @Override
    public YearMonth convert(String source) {
        return YearMonth.parse(source);
    }
}
