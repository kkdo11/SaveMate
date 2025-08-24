package kopo.newproject.service;

import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;

import java.util.List;

/**
 * 사용자 그룹별 평균 소비 데이터 집계와 관련된 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 * <p>
 * 인터페이스를 사용하는 이유:
 * 1.  느슨한 결합(Loose Coupling): 컨트롤러는 이 인터페이스에만 의존하므로, 실제 구현 클래스(Impl)가 변경되어도 컨트롤러 코드는 영향을 받지 않습니다.
 * 2.  테스트 용이성: 단위 테스트 시, 실제 구현 대신 Mock(가짜) 객체를 쉽게 주입할 수 있습니다.
 * 3.  AOP 적용: Spring AOP는 인터페이스 기반의 프록시를 생성하여 트랜잭션, 로깅 등 공통 기능을 적용하기 용이합니다.
 */
public interface IAggregatedSpendingService {

    /**
     * 모든 사용자의 지출 데이터를 집계하여 성별/연령대별 평균 소비 데이터를 생성하고 DB에 저장(캐시)합니다.
     * 이 메소드는 주기적으로(예: 매일 새벽) 스케줄러에 의해 호출되는 것을 상정하고 설계된 무거운 작업입니다.
     *
     * @return 성공적으로 집계 및 저장된 평균 소비 데이터 목록
     * @throws Exception 데이터 처리 중 발생할 수 있는 모든 예외. (실제 운영 코드에서는 더 구체적인 예외를 정의하는 것이 좋습니다.)
     */
    List<AggregatedSpendingEntity> aggregateAndCacheSpendingData() throws Exception;

    /**
     * 특정 성별 및 연령대 그룹의 평균 소비 데이터를 조회합니다.
     * 이 메소드는 위 'aggregateAndCacheSpendingData'에 의해 미리 계산된 데이터를 빠르게 읽어오는 역할을 합니다.
     *
     * @param gender   조회할 성별 (예: "M" 또는 "F")
     * @param ageGroup 조회할 연령대 (예: "20대", "30대")
     * @return 해당 그룹의 집계된 평균 소비 데이터. 데이터가 없을 경우 null을 반환할 수 있습니다.
     * @throws Exception 데이터 조회 중 발생할 수 있는 모든 예외.
     */
    AggregatedSpendingEntity getAggregatedSpendingByGroup(String gender, String ageGroup) throws Exception;
}

