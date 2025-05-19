package kopo.newproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.YearMonth;
import java.util.Arrays;

@Configuration


public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new YearMonthReadConverter(),
                new YearMonthWriteConverter()
        ));
    }

    public static class YearMonthReadConverter implements Converter<String, YearMonth> {
        @Override
        public YearMonth convert(String source) {
            return YearMonth.parse(source);
        }
    }

    public static class YearMonthWriteConverter implements Converter<YearMonth, String> {
        @Override
        public String convert(YearMonth source) {
            return source.toString();
        }
    }
}
