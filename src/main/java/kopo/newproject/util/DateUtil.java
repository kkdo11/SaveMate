package kopo.newproject.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 날짜 및 시간 형식 변환과 관련된 유틸리티 클래스.
 * <p>
 * 현재 날짜/시간을 특정 형식으로 변환하거나, Unix 타임스탬프를 읽기 쉬운 날짜/시간 문자열로 변환하는
 * 정적 메소드들을 제공합니다.
 * <p>
 * (NOTE: 이 클래스는 {@code java.util.Date}와 {@code java.time} API를 혼용하고 있습니다.
 * 최신 Java 애플리케이션에서는 {@code java.time} 패키지(LocalDate, LocalDateTime, Instant 등)만을
 * 사용하는 것이 권장됩니다. 이는 더 나은 불변성, 스레드 안전성, 명확한 의미를 제공합니다.)
 */
public class DateUtil {

    /**
     * 현재 날짜와 시간을 지정된 형식으로 반환합니다.
     * <p>
     * {@code java.util.Date}와 {@code SimpleDateFormat}을 사용합니다.
     *
     * @param fm 날짜/시간 출력 형식 (예: "yyyy-MM-dd HH:mm:ss")
     * @return 지정된 형식으로 포맷된 현재 날짜/시간 문자열
     */
    public static String getDateTime(String fm) {
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat(fm);
        return date.format(today);
    }

    /**
     * 현재 날짜를 기본 형식("yyyy.MM.dd")으로 반환합니다.
     * <p>
     * {@link #getDateTime(String)} 메소드의 오버로드 버전입니다.
     *
     * @return "yyyy.MM.dd" 형식으로 포맷된 현재 날짜 문자열
     */
    public static String getDateTime() {
        return getDateTime("yyyy.MM.dd");
    }

    /**
     * Unix 타임스탬프(초 단위)를 "yyyy-MM-dd HH:mm:ss" 형식의 날짜/시간 문자열로 변환합니다.
     * <p>
     * {@code java.time} API를 사용하여 Unix 타임스탬프를 {@link Instant}로 변환하고,
     * 시스템 기본 시간대를 사용하여 포맷합니다.
     *
     * @param time 변환할 Unix 타임스탬프 ({@code Object} 타입으로 전달되지만 {@code Integer}로 예상됨)
     * @return 포맷된 날짜/시간 문자열
     */
    public static String getLongDateTime(Object time) {
        return getLongDateTime((Integer) time, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Unix 타임스탬프(초 단위)를 "yyyy-MM-dd HH:mm:ss" 형식의 날짜/시간 문자열로 변환합니다.
     * <p>
     * {@link #getLongDateTime(Integer, String)} 메소드의 오버로드 버전입니다.
     *
     * @param time 변환할 Unix 타임스탬프 ({@code Integer} 타입)
     * @return 포맷된 날짜/시간 문자열
     */
    public static String getLongDateTime(Integer time) {
        return getLongDateTime(time, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Unix 타임스탬프(초 단위)를 지정된 형식의 날짜/시간 문자열로 변환합니다.
     * <p>
     * {@link #getLongDateTime(Integer, String)} 메소드의 오버로드 버전입니다.
     *
     * @param time 변환할 Unix 타임스탬프 ({@code Object} 타입으로 전달되지만 {@code Integer}로 예상됨)
     * @param fm   날짜/시간 출력 형식
     * @return 포맷된 날짜/시간 문자열
     */
    public static String getLongDateTime(Object time, String fm) {
        return getLongDateTime((Integer) time, fm);
    }

    /**
     * Unix 타임스탬프(초 단위)를 지정된 형식의 날짜/시간 문자열로 변환합니다.
     * <p>
     * {@code Instant.ofEpochSecond()}를 사용하여 Unix 타임스탬프를 {@link Instant}로 변환하고,
     * {@link DateTimeFormatter}와 시스템 기본 시간대를 사용하여 포맷합니다.
     *
     * @param time 변환할 Unix 타임스탬프 ({@code Integer} 타입)
     * @param fm   날짜/시간 출력 형식
     * @return 포맷된 날짜/시간 문자열
     */
    public static String getLongDateTime(Integer time, String fm) {
        Instant instant = Instant.ofEpochSecond(time);
        return DateTimeFormatter.ofPattern(fm)
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}