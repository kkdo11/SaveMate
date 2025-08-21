package kopo.newproject.controller;

import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;
import kopo.newproject.service.IAggregatedSpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 내부 사용자 데이터 기반 소비 비교 관련 API 컨트롤러
 */
@RequiredArgsConstructor
@RequestMapping("/api/aggregated-spending")
@RestController
public class AggregatedSpendingController {

    private static final Logger log = LoggerFactory.getLogger(AggregatedSpendingController.class);

    private final IAggregatedSpendingService aggregatedSpendingService;

    /**
     * 특정 성별 및 연령대의 평균 소비 데이터와 비교하기 위한 엔드포인트
     * @param gender 비교할 성별
     * @param ageGroup 비교할 연령대
     * @return 집계된 소비 데이터 (없을 경우 404 Not Found)
     */
    @GetMapping("/compare")
    public ResponseEntity<AggregatedSpendingEntity> getAggregatedSpendingForCompare(
            @RequestParam("gender") String gender,
            @RequestParam("ageGroup") String ageGroup) {
        log.info("START: getAggregatedSpendingForCompare - gender: {}, ageGroup: {}", gender, ageGroup);

        try {
            // 서비스 호출하여 데이터 조회
            AggregatedSpendingEntity aggregatedSpending = aggregatedSpendingService.getAggregatedSpendingByGroup(gender, ageGroup);

            // 데이터 존재 여부 확인
            if (aggregatedSpending == null) {
                log.warn("WARN: Aggregated spending data not found for gender: {}, ageGroup: {}", gender, ageGroup);
                return ResponseEntity.notFound().build(); // 데이터가 없으면 404 응답
            }

            log.info("SUCCESS: Found aggregated spending data with ID: {}", aggregatedSpending.getId());
            return ResponseEntity.ok(aggregatedSpending); // 성공 시 200 OK와 함께 데이터 반환

        } catch (Exception e) {
            log.error("ERROR: Failed to get aggregated spending data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build(); // 서버 오류 시 500 응답
        } finally {
            log.info("END: getAggregatedSpendingForCompare");
        }
    }

    /**
     * 전체 사용자 소비 데이터를 집계하여 MongoDB에 저장하는 작업을 수동으로 트리거하는 엔드포인트
     * @return 작업 성공/실패 메시지
     */
    @GetMapping("/aggregate")
    public ResponseEntity<String> aggregateSpendingData() {
        log.info("START: aggregateSpendingData");
        try {
            // 서비스 호출하여 데이터 집계 및 저장
            aggregatedSpendingService.aggregateAndCacheSpendingData();
            log.info("SUCCESS: Aggregated spending data successfully.");
            return ResponseEntity.ok("Aggregated spending data successfully."); // 성공 시 200 OK

        } catch (Exception e) {
            log.error("ERROR: Failed to aggregate spending data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to aggregate spending data."); // 실패 시 500 응답
        } finally {
            log.info("END: aggregateSpendingData");
        }
    }
}