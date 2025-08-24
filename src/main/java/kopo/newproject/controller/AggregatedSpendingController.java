package kopo.newproject.controller;

import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;
import kopo.newproject.service.IAggregatedSpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내부 사용자 데이터 기반 소비 비교 관련 API 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @RestController} - @Controller와 @ResponseBody를 합친 어노테이션.
 * 이 컨트롤러의 모든 메소드는 뷰(HTML)가 아닌 JSON, XML 등의 데이터(body)를 직접 반환합니다.
 * {@code @RequestMapping("/api/aggregated-spending")} - 이 컨트롤러의 모든 메소드는 '/api/aggregated-spending' 경로 하위에 매핑됩니다.
 * {@code @RequiredArgsConstructor} - final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입합니다.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/aggregated-spending")
@RestController
public class AggregatedSpendingController {

    private final IAggregatedSpendingService aggregatedSpendingService;

    /**
     * 특정 성별 및 연령대의 평균 소비 데이터를 조회하는 API.
     *
     * @param gender   비교할 성별 (URL 쿼리 파라미터: ?gender=남성)
     * @param ageGroup 비교할 연령대 (URL 쿼리 파라미터: &ageGroup=30대)
     * @return {@link ResponseEntity} - 조회된 집계 데이터 또는 에러 상태를 포함하는 HTTP 응답.
     *         - 성공: 200 OK와 함께 {@link AggregatedSpendingEntity} 데이터 반환.
     *         - 데이터 없음: 404 Not Found 반환.
     *         - 서버 오류: 500 Internal Server Error 반환.
     */
    @GetMapping("/compare")
    public ResponseEntity<AggregatedSpendingEntity> getAggregatedSpendingForCompare(
            @RequestParam("gender") String gender,
            @RequestParam("ageGroup") String ageGroup) {
        log.info("▶▶▶ [API Start] getAggregatedSpendingForCompare | gender: {}, ageGroup: {}", gender, ageGroup);

        try {
            // 서비스 레이어를 호출하여 비즈니스 로직 수행
            AggregatedSpendingEntity aggregatedSpending = aggregatedSpendingService.getAggregatedSpendingByGroup(gender, ageGroup);

            // 서비스로부터 받은 결과에 따라 분기 처리
            if (aggregatedSpending == null) {
                // 데이터가 없는 경우, 클라이언트에게 '찾을 수 없음'을 명확히 알림
                log.warn("조회된 집계 데이터 없음 | gender: {}, ageGroup: {}", gender, ageGroup);
                return ResponseEntity.notFound().build();
            }

            // 성공적으로 데이터를 찾은 경우
            log.info("집계 데이터 조회 성공 | ID: {}", aggregatedSpending.getId());
            return ResponseEntity.ok(aggregatedSpending);

        } catch (Exception e) {
            // 로직 수행 중 예기치 않은 에러 발생 시
            log.error("집계 데이터 조회 중 에러 발생", e);
            return ResponseEntity.internalServerError().build();
        } finally {
            log.info("◀◀◀ [API End] getAggregatedSpendingForCompare");
        }
    }

    /**
     * 전체 사용자 소비 데이터를 집계하여 MongoDB에 저장하는 작업을 수동으로 실행(trigger)하는 API.
     * 주로 개발 및 테스트 목적으로 사용되며, 스케줄러에 의해 자동으로 실행되는 작업을 즉시 실행하고 싶을 때 호출합니다.
     *
     * @return {@link ResponseEntity} - 작업 성공 또는 실패 메시지를 포함하는 HTTP 응답.
     */
    @GetMapping("/aggregate")
    public ResponseEntity<String> aggregateSpendingData() {
        log.info("▶▶▶ [API Start] aggregateSpendingData");
        try {
            // 데이터 집계 서비스 실행
            aggregatedSpendingService.aggregateAndCacheSpendingData();

            log.info("데이터 집계 작업 성공");
            return ResponseEntity.ok("Data aggregation triggered successfully.");

        } catch (Exception e) {
            log.error("데이터 집계 작업 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("Failed to trigger data aggregation.");
        } finally {
            log.info("◀◀◀ [API End] aggregateSpendingData");
        }
    }
}
