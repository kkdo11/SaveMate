package kopo.newproject.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

/**
 * 연령 계산 및 연령대 분류와 관련된 유틸리티 클래스.
 * <p>
 * 생년월일 문자열을 기반으로 현재 나이(만 나이)를 계산하고,
 * 계산된 나이를 특정 연령대(예: "20대")로 분류하는 정적 메소드들을 제공합니다.
 */
@Slf4j
public class AgeCalculator {

    /**
     * 생년월일 문자열로부터 현재 연령(만 나이)을 계산합니다.
     * <p>
     * 입력된 생년월일 문자열은 "YYYY-MM-DD" 형식이어야 합니다.
     * {@link LocalDate#parse(CharSequence)}를 사용하여 문자열을 날짜 객체로 변환하고,
     * {@link Period#between(LocalDate, LocalDate)}을 사용하여 두 날짜 사이의 기간을 계산합니다.
     *
     * @param birthDateString "YYYY-MM-DD" 형식의 생년월일 문자열
     * @return 계산된 연령 (만 나이). 유효하지 않은 형식이거나 null일 경우 -1을 반환합니다.
     */
    public static int calculateAge(String birthDateString) {
        log.info("▶▶▶ [Util] calculateAge | 생년월일: {}", birthDateString);
        // 입력값 유효성 검사
        if (birthDateString == null || birthDateString.trim().isEmpty()) {
            log.warn("생년월일 문자열이 null이거나 비어 있습니다. -1을 반환합니다.");
            return -1;
        }

        try {
            // 문자열을 LocalDate 객체로 파싱
            LocalDate birthDate = LocalDate.parse(birthDateString);
            LocalDate currentDate = LocalDate.now(); // 현재 날짜

            // Period.between을 사용하여 두 날짜 사이의 기간(년, 월, 일)을 계산하고, 연도만 가져와 나이로 사용
            int age = Period.between(birthDate, currentDate).getYears();
            log.info("◀◀◀ [Util] calculateAge | 계산된 연령: {} (생년월일: {})", age, birthDateString);
            return age;

        } catch (DateTimeParseException e) {
            // 날짜 형식이 잘못되었을 경우 로그를 남기고 -1 반환
            log.error("유효하지 않은 날짜 형식입니다. 'YYYY-MM-DD' 형식으로 전달해주세요. 입력값: '{}'", birthDateString, e);
            return -1;
        }
    }

    /**
     * 연령을 기반으로 연령대 문자열을 반환합니다.
     * <p>
     * 예시: 10대, 20대, 30대 등.
     *
     * @param age 계산된 연령
     * @return 연령대 문자열. 유효하지 않은 연령(음수)일 경우 "알 수 없음"을 반환합니다.
     */
    public static String getAgeGroup(int age) {
        log.info("▶▶▶ [Util] getAgeGroup | 연령: {}", age);
        String ageGroup;
        if (age < 0) {
            log.warn("유효하지 않은 연령({})이 제공되었습니다. 연령대를 결정할 수 없습니다.", age);
            ageGroup = "알 수 없음";
        } else if (age < 20) {
            ageGroup = "10대";
        } else if (age < 30) {
            ageGroup = "20대";
        } else if (age < 40) {
            ageGroup = "30대";
        } else if (age < 50) {
            ageGroup = "40대";
        } else if (age < 60) {
            ageGroup = "50대";
        } else if (age < 70) {
            ageGroup = "60대";
        } else {
            ageGroup = "70대 이상";
        }
        log.info("◀◀◀ [Util] getAgeGroup | 연령대: {}", ageGroup);
        return ageGroup;
    }
}
