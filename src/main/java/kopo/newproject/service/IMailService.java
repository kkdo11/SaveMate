package kopo.newproject.service;

import kopo.newproject.dto.MailDTO;

/**
 * 이메일 발송 관련 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 */
public interface IMailService {

    /**
     * 지정된 수신자에게 이메일을 발송합니다.
     *
     * @param mailDTO 수신자, 제목, 내용을 담고 있는 DTO
     * @return 성공: 1, 실패: 0
     * (NOTE: 현대적인 설계에서는 int 대신 boolean을 반환하거나, 실패 시 예외를 던지는 방식을 더 선호합니다.)
     */
    int doSendMail(MailDTO mailDTO);

    /**
     * 이메일 인증에 사용할 랜덤 인증 코드를 생성합니다.
     *
     * @return 생성된 랜덤 문자열 (예: "123456")
     */
    String generateVerificationCode();

    /**
     * 특정 이메일 주소로 인증 코드를 발송합니다.
     * 내부적으로 정해진 템플릿을 사용하여 메일 내용을 구성하고 doSendMail을 호출할 수 있습니다.
     *
     * @param toMail           인증 코드를 받을 이메일 주소
     * @param verificationCode 발송할 인증 코드
     * @return 성공: 1, 실패: 0
     */
    int sendVerificationMail(String toMail, String verificationCode);
}