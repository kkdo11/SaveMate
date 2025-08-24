package kopo.newproject.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * AOP(Aspect-Oriented Programming)를 이용한 로깅 처리를 담당하는 클래스.
 * {@code @Aspect} - 이 클래스가 AOP의 Aspect(공통 기능 모듈)임을 나타냅니다.
 * {@code @Component} - Spring 컨테이너가 이 클래스를 Bean으로 등록하여 관리하도록 합니다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * kopo.newproject.controller 패키지와 그 하위 패키지에 속한 모든 클래스의 모든 메소드를 대상으로 하는 Pointcut.
     * 컨트롤러 계층의 메소드 실행을 감지하기 위해 사용됩니다.
     */
    @Pointcut("execution(* kopo.newproject.controller..*.*(..))")
    public void controller() {
    }

    /**
     * kopo.newproject.service 패키지와 그 하위 패키지에 속한 모든 클래스의 모든 메소드를 대상으로 하는 Pointcut.
     * 서비스 계층의 메소드 실행을 감지하기 위해 사용됩니다.
     */
    @Pointcut("execution(* kopo.newproject.service..*.*(..))")
    public void service() {
    }

    /**
     * controller() 또는 service() Pointcut에 해당하는 메소드 실행 전후에 로직을 추가하는 Around Advice.
     * 메소드 실행 시간, 요청 정보, 반환 값 등을 종합적으로 로깅합니다.
     *
     * @param joinPoint 프록시된 원본 메소드에 대한 정보를 담고 있으며, 원본 메소드를 실행할 수 있는 기능을 제공합니다.
     * @return 원본 메소드의 실행 결과
     * @throws Throwable 원본 메소드 실행 중 발생할 수 있는 예외
     */
    @Around("controller() || service()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 메소드 실행 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // 실행되는 클래스와 메소드 이름, 그리고 전달된 인자들을 로깅합니다.
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = join.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("▶▶▶ [Request] {}.{}({})", className, methodName, Arrays.toString(args));

        // HTTP 요청 정보를 로깅합니다. (웹 요청이 있는 경우에만 해당)
        // RequestContextHolder를 통해 현재 스레드에 바인딩된 HttpServletRequest 객체를 가져옵니다.
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            log.info("    [HTTP Info] Method: {}, URI: {}", request.getMethod(), request.getRequestURI());
        }

        // joinPoint.proceed()를 호출하여 원본 메소드를 실행합니다.
        Object result = joinPoint.proceed();

        // 메소드 실행 종료 시간 기록 및 총 실행 시간 계산
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;

        // 메소드 실행 결과와 총 실행 시간을 로깅합니다.
        log.info("◀◀◀ [Response] {}.{}() | Result: {} | Execution Time: {}ms", className, methodName, result, timeTaken);

        // 원본 메소드의 결과를 반환합니다.
        return result;
    }
}
