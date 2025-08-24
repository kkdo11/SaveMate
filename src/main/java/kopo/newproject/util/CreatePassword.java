package kopo.newproject.util;

public class CreatePassword {

    /**
     * 임시 비밀번호 생성과 관련된 유틸리티 클래스.
     * <p>
     * 주로 비밀번호 재설정 기능 등에서 사용자에게 임시 비밀번호를 발급할 때 사용됩니다.
     * 생성된 임시 비밀번호는 보안을 위해 사용자에게 즉시 변경하도록 안내해야 합니다.
     */
    public static String createTempPassword() {
        int length = 10; // 임시 비밀번호의 길이
        // 비밀번호에 사용될 문자 집합 (영문 대소문자, 숫자)
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder(); // 비밀번호를 효율적으로 구축하기 위한 StringBuilder

        // 지정된 길이만큼 반복하여 무작위 문자 선택
        for (int i = 0; i < length; i++) {
            // charSet 길이 내에서 무작위 인덱스 생성
            int randIndex = (int) (Math.random() * charSet.length());
            password.append(charSet.charAt(randIndex));
        }

        return password.toString(); // StringBuilder를 String으로 변환하여 반환
    }
}