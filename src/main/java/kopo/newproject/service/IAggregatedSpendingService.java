package kopo.newproject.service;

import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;

import java.util.List;

public interface IAggregatedSpendingService {

    /**
     * 모든 사용자의 지출 데이터를 집계하여 성별/연령대별 평균 소비 데이터를 캐시(DB)에 저장합니다.
     * @return 집계된 평균 소비 데이터 목록
     * @throws Exception
     */
    List<AggregatedSpendingEntity> aggregateAndCacheSpendingData() throws Exception;

    /**
     * 특정 성별 및 연령대 그룹의 평균 소비 데이터를 조회합니다.
     * @param gender 성별 (M 또는 F)
     * @param ageGroup 연령대 (예: "20대")
     * @return 해당 그룹의 평균 소비 데이터
     * @throws Exception
     */
    AggregatedSpendingEntity getAggregatedSpendingByGroup(String gender, String ageGroup) throws Exception;
}

