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

@Slf4j
@RestController
@RequestMapping("/spendingAPI")
@RequiredArgsConstructor
public class SpendingAPIController {

    private final ISpendingService spendingService;

    // 현재 사용자 ID를 얻는 메서드
    private String getCurrentUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("현재 사용자 ID: {}", userId);
        return userId;
    }

    // 지출 내역 조회 (월별, 카테고리별)
    @GetMapping
    public ResponseEntity<?> getSpendingList(@RequestParam(required = false) String month,
                                             @RequestParam(required = false) String category) {
        log.info("[API] 지출 내역 조회 요청됨, month={}, category={}", month, category);

        try {
            YearMonth yearMonth = null;
            if (month != null && !month.isBlank()) {
                yearMonth = YearMonth.parse(month);
            }

            // category가 비어 있으면 모든 카테고리를 조회하도록 처리
            List<SpendingEntity> spendings;
            if (category == null || category.isBlank()) {
                spendings = spendingService.getSpendings(getCurrentUserId(), yearMonth, null);  // category를 null로 설정
            } else {
                spendings = spendingService.getSpendings(getCurrentUserId(), yearMonth, category);
            }

            if (spendings.isEmpty()) {
                log.warn("조회된 지출 내역이 없습니다.");
                return ResponseEntity.ok(Collections.emptyList());  // 빈 리스트 반환
            }

            log.info("조회된 지출 내역 수: {}", spendings.size());
            return ResponseEntity.ok(spendings);

        } catch (DateTimeParseException e) {
            log.error("날짜 형식 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body("잘못된 날짜 형식입니다. 'yyyy-MM' 형식으로 전달해주세요.");
        } catch (Exception e) {
            log.error("지출 내역 조회 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지출 내역 조회 실패");
        }
    }

    // ✅ 지출 단건 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpendingById(@PathVariable String id) {
        try {
            SpendingEntity spending = spendingService.getSpendingById(getCurrentUserId(), id);
            if (spending != null) {
                return ResponseEntity.ok(spending);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("지출 내역 없음");
            }
        } catch (Exception e) {
            log.error("지출 단건 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패");
        }
    }







    // 지출 내역 저장
    @PostMapping
    public ResponseEntity<?> createSpending(@RequestBody @Valid SpendingRequest request) {
        log.info("[API] 지출 내역 저장 요청: {}", request);
        try {
            SpendingEntity saved = spendingService.saveSpending(getCurrentUserId(), request);
            log.info("지출 내역 저장 성공: {}", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("지출 내역 저장 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지출 내역 저장 실패");
        }
    }

    // 지출 내역 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSpending(@PathVariable String id, @RequestBody @Valid SpendingRequest request) {
        log.info("[API] 지출 수정 요청: id={}, request={}", id, request);
        try {
            boolean updated = spendingService.updateSpending(getCurrentUserId(), id, request);

            if (updated) {
                log.info("지출 내역 수정 완료: id={}", id);
                return ResponseEntity.ok().body("{\"message\": \"수정 완료\"}");
            } else {
                log.warn("수정 실패: 지출 내역이 존재하지 않음, id={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"지출 내역 없음\"}");
            }
        } catch (Exception e) {
            log.error("지출 내역 수정 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "지출 수정 실패"));

        }
    }

    // 지출 내역 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpending(@PathVariable String id) {
        log.info("[API] 지출 삭제 요청: id={}", id);
        try {
            boolean deleted = spendingService.deleteSpending(getCurrentUserId(), id);
            if (deleted) {
                log.info("지출 내역 삭제 완료: id={}", id);
                return ResponseEntity.ok("{\"message\": \"삭제 완료\"}");
            } else {
                log.warn("삭제 실패: 지출 내역이 존재하지 않음, id={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"지출 내역 없음\"}");
            }
        } catch (Exception e) {
            log.error("지출 내역 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"지출 삭제 실패\"}");
        }
    }
}