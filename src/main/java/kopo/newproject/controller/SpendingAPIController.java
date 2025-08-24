package kopo.newproject.controller;

import jakarta.validation.Valid;
import kopo.newproject.dto.SpendingRequest;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 지출(Spending) 내역 CRUD 관련 API 요청을 처리하는 컨트롤러.
 */
@Slf4j
@RestController
@RequestMapping("/api/spendings") // RESTful API 경로 규칙에 따라 /spendingAPI -> /api/spendings 로 수정
@RequiredArgsConstructor
public class SpendingAPIController {

    private final ISpendingService spendingService;

    /**
     * Spring Security 컨텍스트에서 현재 인증된 사용자의 ID를 안전하게 가져오는 헬퍼 메소드.
     * @return 현재 로그인된 사용자의 ID
     */
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 지출 내역을 조건에 따라 조회하는 API. (월별, 카테고리별 필터링 가능)
     * @param month    조회할 연월 (YYYY-MM 형식, Optional)
     * @param category 조회할 카테고리 (Optional)
     * @return 조회된 지출 내역 목록을 포함하는 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<?> getSpendingList(@RequestParam(required = false) String month,
                                             @RequestParam(required = false) String category) {
        log.info("▶▶▶ [API Start] getSpendingList | month: {}, category: {}", month, category);
        try {
            YearMonth yearMonth = null;
            if (month != null && !month.isBlank()) {
                yearMonth = YearMonth.parse(month);
            }

            List<SpendingEntity> spendings = spendingService.getSpendings(getCurrentUserId(), yearMonth, category);
            log.info("지출 내역 {}건 조회 성공", spendings.size());
            return ResponseEntity.ok(spendings);

        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식으로 요청 | input: {}", month, e);
            return ResponseEntity.badRequest().body("잘못된 날짜 형식입니다. 'yyyy-MM' 형식으로 전달해주세요.");
        } catch (Exception e) {
            log.error("지출 내역 조회 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지출 내역 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getSpendingList");
        }
    }

    /**
     * ID를 통해 특정 지출 내역 단건을 조회하는 API.
     * @param id 조회할 지출 내역의 ID (URL 경로 변수)
     * @return 조회된 지출 내역 또는 404 Not Found 응답
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpendingById(@PathVariable String id) {
        log.info("▶▶▶ [API Start] getSpendingById | id: {}", id);
        try {
            SpendingEntity spending = spendingService.getSpendingById(getCurrentUserId(), id);
            if (spending != null) {
                log.info("ID 기준 지출 내역 조회 성공 | id: {}", id);
                return ResponseEntity.ok(spending);
            } else {
                log.warn("조회할 지출 내역 없음 | id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 ID의 지출 내역이 없습니다.");
            }
        } catch (Exception e) {
            log.error("ID 기준 지출 내역 조회 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getSpendingById");
        }
    }

    /**
     * 새로운 지출 내역을 저장하는 API.
     * {@code @Valid} 어노테이션을 통해 SpendingRequest DTO에 정의된 유효성 검사(예: NotNull)를 자동으로 수행합니다.
     * @param request HTTP 요청 Body에 담겨온 지출 생성 정보 (JSON)
     * @return 생성된 지출 내역 정보를 포함하는 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<?> createSpending(@RequestBody @Valid SpendingRequest request) {
        log.info("▶▶▶ [API Start] createSpending");
        try {
            SpendingEntity saved = spendingService.saveSpending(getCurrentUserId(), request);
            log.info("지출 내역 저장 성공 | id: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("지출 내역 저장 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지출 내역 저장에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] createSpending");
        }
    }

    /**
     * 기존 지출 내역을 수정하는 API.
     * @param id      수정할 지출 내역의 ID (URL 경로 변수)
     * @param request 수정할 지출 정보 (JSON)
     * @return 성공 또는 실패 메시지를 담은 ResponseEntity
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSpending(@PathVariable String id, @RequestBody @Valid SpendingRequest request) {
        log.info("▶▶▶ [API Start] updateSpending | id: {}", id);
        try {
            boolean updated = spendingService.updateSpending(getCurrentUserId(), id, request);
            if (updated) {
                log.info("지출 내역 수정 성공 | id: {}", id);
                // NOTE: 성공 시, 메시지 대신 수정된 객체 전체를 반환하거나, 단순히 200 OK 상태 코드만 반환하는 것이 일반적인 REST API 스타일입니다.
                return ResponseEntity.ok(Map.of("message", "수정 완료"));
            } else {
                log.warn("수정할 지출 내역 없음 | id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "해당 ID의 지출 내역이 없습니다."));
            }
        } catch (Exception e) {
            log.error("지출 내역 수정 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "지출 내역 수정에 실패했습니다."));
        } finally {
            log.info("◀◀◀ [API End] updateSpending");
        }
    }

    /**
     * 특정 지출 내역을 삭제하는 API.
     * @param id 삭제할 지출 내역의 ID (URL 경로 변수)
     * @return 성공 또는 실패 메시지를 담은 ResponseEntity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpending(@PathVariable String id) {
        log.info("▶▶▶ [API Start] deleteSpending | id: {}", id);
        try {
            boolean deleted = spendingService.deleteSpending(getCurrentUserId(), id);
            if (deleted) {
                log.info("지출 내역 삭제 성공 | id: {}", id);
                // NOTE: 성공적인 삭제(DELETE) 요청에는 Body 없이 204 No Content 상태 코드를 반환하는 것이 일반적인 REST API 스타일입니다.
                return ResponseEntity.noContent().build();
            } else {
                log.warn("삭제할 지출 내역 없음 | id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "해당 ID의 지출 내역이 없습니다."));
            }
        } catch (Exception e) {
            log.error("지출 내역 삭제 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "지출 내역 삭제에 실패했습니다."));
        } finally {
            log.info("◀◀◀ [API End] deleteSpending");
        }
    }
}
