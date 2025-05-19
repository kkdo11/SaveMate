package kopo.newproject.controller;

import kopo.newproject.dto.BudgetDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.service.IBudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/budgetAPI")
@RequiredArgsConstructor
public class BudgetAPIController {

    private final IBudgetService budgetService;

    private String getCurrentUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return userId;
    }

    // 예산 생성
    @PostMapping
    public ResponseEntity<BudgetEntity> createBudget(
            @RequestBody BudgetDTO dto
    ) {
        try {
            String userId = getCurrentUserId();
            log.info("예산 생성 요청: userId={}, dto={}", userId, dto);  // 예산 생성 요청 로그 출력
            BudgetEntity created = budgetService.createBudget(userId, dto);
            log.info("예산 생성 성공: {}", created);  // 예산 생성 성공 로그 출력
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("예산 생성 중 오류 발생: {}", e.getMessage(), e);  // 예외 발생 시 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 예산 수정
    @PutMapping("/{budgetId}")
    public ResponseEntity<Void> updateBudget(
            @PathVariable Long budgetId,
            @RequestBody BudgetDTO dto
    ) {
        try {
            String userId = getCurrentUserId();
            log.info("예산 수정 요청: userId={}, budgetId={}, dto={}", userId, budgetId, dto);  // 예산 수정 요청 로그 출력
            boolean success = budgetService.updateBudget(userId, budgetId, dto);
            if (success) {
                log.info("예산 수정 성공: budgetId={}", budgetId);  // 예산 수정 성공 로그 출력
                return ResponseEntity.ok().build();
            } else {
                log.warn("예산 수정 실패: 예산이 존재하지 않음, budgetId={}", budgetId);  // 예산 수정 실패 로그 출력
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("예산 수정 중 오류 발생: {}", e.getMessage(), e);  // 예외 발생 시 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 예산 삭제
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long budgetId
    ) {
        try {
            String userId = getCurrentUserId();
            log.info("예산 삭제 요청: userId={}, budgetId={}", userId, budgetId);  // 예산 삭제 요청 로그 출력
            boolean success = budgetService.deleteBudget(userId, budgetId);
            if (success) {
                log.info("예산 삭제 성공: budgetId={}", budgetId);  // 예산 삭제 성공 로그 출력
                return ResponseEntity.ok().build();
            } else {
                log.warn("예산 삭제 실패: 예산이 존재하지 않음, budgetId={}", budgetId);  // 예산 삭제 실패 로그 출력
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("예산 삭제 중 오류 발생: {}", e.getMessage(), e);  // 예외 발생 시 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 특정 연/월 예산 조회
    @GetMapping("/month")
    public ResponseEntity<BudgetEntity> getBudgetByYearMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        try {
            String userId = getCurrentUserId();
            log.info("특정 연/월 예산 조회 요청: userId={}, year={}, month={}", userId, year, month);  // 예산 조회 요청 로그 출력
            BudgetEntity result = budgetService.getBudgetByUserIdAndYearMonth(userId, year, month);
            if (result != null) {
                log.info("예산 조회 성공: {}", result);  // 예산 조회 성공 로그 출력
                return ResponseEntity.ok(result);
            } else {
                log.warn("예산 조회 실패: 예산이 존재하지 않음, userId={}, year={}, month={}", userId, year, month);  // 예산이 없을 때 로그 출력
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("예산 조회 중 오류 발생: {}", e.getMessage(), e);  // 예외 발생 시 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 필터 조건으로 예산 조회 (userId 없이 사용 가능)
    @GetMapping("/filter")
    public ResponseEntity<?> getBudgetsByFilter(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String category
    ) {
        try {
            log.info("필터 조건 예산 조회 요청: month={}, category={}", month, category);  // 필터 조건 예산 조회 로그 출력
            List<BudgetEntity> result;
            if (month == null && (category == null || category.isBlank())) {
                log.info("전체 예산 조회");  // 전체 조회 로그 출력
                result = budgetService.findAll();
            } else {
                log.info("조건에 맞는 예산 조회: month={}, category={}", month, category);  // 조건 조회 로그 출력
                result = budgetService.findByMonthAndCategory(month, category);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("예산 조회 중 오류 발생: {}", e.getMessage(), e);  // 예외 발생 시 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    // 유저별 전체 예산 목록 조회
    @GetMapping
    public ResponseEntity<List<BudgetEntity>> getBudgetsByUserId() {
        try {
            String userId = getCurrentUserId();
            log.info("유저별 예산 목록 조회 요청: userId={}", userId);  // 유저별 예산 목록 조회 로그 출력
            List<BudgetEntity> budgets = budgetService.getBudgetsByUserId(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            log.error("예산 목록 조회 중 오류 발생: {}", e.getMessage(), e);  // 예외 발생 시 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{budgetId}") //단건 조회
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable Long budgetId) {
        try {
            String userId = getCurrentUserId();
            BudgetDTO budget = budgetService.getBudgetById(userId, budgetId);
            if (budget == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            log.error("예산 단건 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
