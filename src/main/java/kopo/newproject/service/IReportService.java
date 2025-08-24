package kopo.newproject.service;

import kopo.newproject.dto.MonthlyReportDTO;

import java.time.YearMonth;

/**
 * 월간 재무 리포트 생성 및 발송과 관련된 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 */
public interface IReportService {

    /**
     * 특정 사용자의 특정 월에 대한 리포트 데이터를 생성합니다.
     * <p>
     * 이 메소드는 해당 월의 지출, 예산, 목표 달성률 등 리포트에 필요한 모든 데이터를
     * 각 서비스에서 조회하고 가공하여 하나의 DTO({@link MonthlyReportDTO})로 집계하는 역할을 합니다.
     *
     * @param userId      리포트를 생성할 사용자 ID
     * @param reportMonth 리포트 대상 연월 (예: 2025-08)
     * @return 모든 데이터가 채워진 리포트 데이터 DTO
     * @throws Exception 데이터 조회 및 처리 중 발생할 수 있는 예외
     */
    MonthlyReportDTO generateMonthlyReportData(String userId, YearMonth reportMonth) throws Exception;

    /**
     * 특정 사용자에게 특정 월의 리포트를 생성하고 즉시 이메일로 발송합니다.
     * <p>
     * 내부적으로 {@link #generateMonthlyReportData}를 호출하여 리포트 데이터를 생성한 후,
     * 이메일 서비스(IMailService)를 통해 발송하는 과정을 한번에 처리합니다.
     * 주로 테스트나 특정 사용자 재발송에 사용됩니다.
     *
     * @param userId      리포트를 발송할 사용자 ID
     * @param reportMonth 리포트 대상 연월
     * @throws Exception 리포트 생성 및 발송 중 발생할 수 있는 예외
     */
    void generateAndSendReportForUser(String userId, YearMonth reportMonth) throws Exception;

    /**
     * 모든 활성 사용자에게 월간 리포트를 발송하는 스케줄링된 작업을 실행합니다.
     * <p>
     * 이 메소드는 스케줄러에 의해 매월 초에 호출되는 것을 상정합니다.
     * 내부적으로 모든 사용자 목록을 조회하고, 각 사용자에 대해 {@link #generateAndSendReportForUser}를 호출하는 로직을 수행합니다.
     */
    void sendMonthlyReportToAllUsers();

}