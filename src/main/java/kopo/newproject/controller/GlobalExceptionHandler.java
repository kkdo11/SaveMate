package kopo.newproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 애플리케이션 전역에서 발생하는 예외를 통합적으로 처리하는 클래스.
 * <p>
 * {@code @ControllerAdvice} - 이 어노테이션을 통해, 이 클래스는 모든 {@code @Controller} 및 {@code @RestController}에서
 * 발생하는 예외를 감지하고 처리하는 전역 핸들러로 동작합니다.
 * 이를 통해 컨트롤러 코드에서 반복적인 try-catch 블록을 제거하고, 예외 처리 로직을 한 곳에서 관리할 수 있습니다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 가장 일반적인 타입인 {@link Exception}을 처리하는 핸들러.
     * 다른 특정 예외 핸들러에서 처리되지 않은 모든 예외는 이 메소드에서 처리됩니다.
     *
     * @param e 발생한 예외 객체
     * @return 500 Internal Server Error 상태 코드와 에러 메시지를 담은 {@link ResponseEntity}
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception e) {
        // 어떤 컨트롤러에서 어떤 에러가 발생했는지 상세히 로그를 남기는 것이 매우 중요합니다.
        log.error("처리되지 않은 전역 예외 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
    }

    /**
     * {@link NullPointerException}을 특정하여 처리하는 핸들러.
     * ExceptionHandler는 가장 구체적으로 일치하는 타입의 핸들러를 우선적으로 사용합니다.
     *
     * @param e 발생한 NullPointerException 객체
     * @return 400 Bad Request 상태 코드와 에러 메시지를 담은 {@link ResponseEntity}
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException 발생", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("요청 처리 중 오류가 발생했습니다. (Null 참조)");
    }
}
