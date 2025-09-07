package kopo.newproject.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

/**
 * 연령 계산 관련 유틸리티 클래스
 */
@Slf4j
public class AgeCalculator {

    /**
     * 생년월일 문자열로부터 현재 연령을 계산합니다. (만 나이)
     * 생년월일은 "YYYY-MM-DD" 형식이어야 합니다.
     *
     * @param birthDateString "YYYY-MM-DD" 형식의 생년월일 문자열
     * @return 계산된 연령 (만 나이). 유효하지 않은 형식이거나 null일 경우 -1을 반환합니다.
     */
    public static int calculateAge(String birthDateString) {
        // 입력값 유효성 검사
        if (birthDateString == null || birthDateString.trim().isEmpty()) {
            log.warn("Input birthDateString is null or empty.");
            return -1;
        }

        try {
            // 문자열을 LocalDate 객체로 파싱
            LocalDate birthDate = LocalDate.parse(birthDateString);
            LocalDate currentDate = LocalDate.now();

            // Period.between을 사용하여 나이 계산
            int age = Period.between(birthDate, currentDate).getYears();
            log.debug("Calculated age: {} for birthDate: {}", age, birthDateString);
            return age;

        } catch (DateTimeParseException e) {
            // 날짜 형식이 잘못되었을 경우 로그를 남기고 -1 반환
            log.error("Invalid date format for birthDateString: '{}'. Expected format is YYYY-MM-DD.", birthDateString, e);
            return -1;
        }
    }

    /**
     * 연령을 기반으로 연령대 문자열을 반환합니다.
     * 예: 10대, 20대, 30대 등
     *
     * @param age 계산된 연령
     * @return 연령대 문자열. 유효하지 않은 연령(음수)일 경우 "알 수 없음"을 반환합니다.
     */
    public static String getAgeGroup(int age) {
        if (age < 0) {
            log.warn("Invalid age ({}) provided. Cannot determine age group.", age);
            return "Unknown";
        } else if (age < 20) {
            return "10s";
        } else if (age < 30) {
            return "20s";
        } else if (age < 40) {
            return "30s";
        } else if (age < 50) {
            return "40s";
        } else if (age < 60) {
            return "50s";
        } else if (age < 70) {
            return "60s";
        } else {
            return "70s_and_up";
        }
    }
}