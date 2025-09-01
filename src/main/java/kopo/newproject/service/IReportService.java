package kopo.newproject.service;

import kopo.newproject.dto.MonthlyReportDTO;

import java.time.YearMonth;

public interface IReportService {

    /**
     * 특정 사용자의 특정 월에 대한 리포트 데이터를 생성합니다.
     *
     * @param userId      리포트를 생성할 사용자 ID
     * @param reportMonth 리포트 대상 연월
     * @return 채워진 리포트 데이터 DTO
     * @throws Exception 데이터 조회 및 처리 중 발생할 수 있는 예외
     */
    MonthlyReportDTO generateMonthlyReportData(String userId, YearMonth reportMonth) throws Exception;

    /**
     * 특정 사용자에게 특정 월의 리포트를 생성하고 즉시 발송합니다.
     *
     * @param userId      리포트를 발송할 사용자 ID
     * @param reportMonth 리포트 대상 연월
     * @throws Exception 리포트 생성 및 발송 중 발생할 수 있는 예외
     */
    void generateAndSendReportForUser(String userId, YearMonth reportMonth) throws Exception;

    /**
     * 모든 사용자에게 월간 리포트를 발송하는 스케줄링된 작업을 실행합니다.
     */
    void sendMonthlyReportToAllUsers();

}
