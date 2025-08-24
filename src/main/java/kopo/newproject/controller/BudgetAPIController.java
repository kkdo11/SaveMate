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

/**
 * 예산(Budget) CRUD 관련 API 요청을 처리하는 컨트롤러.
 */
@Slf4j
@RestController
@RequestMapping("/api/budgets") // 리소스 이름을 명확히 하기 위해 /budgetAPI -> /api/budgets 로 변경 권장
@RequiredArgsConstructor
public class BudgetAPIController {

    private final IBudgetService budgetService;

    /**
     * Spring Security 컨텍스트에서 현재 인증된 사용자의 ID를 안전하게 가져오는 헬퍼 메소드.
     * @return 현재 로그인된 사용자의 ID
     * @throws AccessDeniedException 사용자가 로그인하지 않은 경우 발생
     */
    private String getCurrentUserId() throws AccessDeniedException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return userId;
    }

    /**
     * 새로운 예산을 생성하는 API.
     * @param budgetDTO HTTP 요청의 Body에 담겨온 예산 생성 정보 (JSON)
     * @return 생성된 예산 정보({@link BudgetEntity})를 포함하는 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<BudgetEntity> createBudget(@RequestBody BudgetDTO budgetDTO) {
        log.info("▶▶▶ [API Start] createBudget");
        try {
            String userId = getCurrentUserId();
            BudgetEntity created = budgetService.createBudget(userId, budgetDTO);
            log.info("예산 생성 성공 | budgetId: {}", created.getBudgetId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("예산 생성 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] createBudget");
        }
    }

    /**
     * 기존 예산을 수정하는 API.
     * @param budgetId 수정할 예산의 ID (URL 경로 변수)
     * @param budgetDTO 수정할 예산 정보 (JSON)
     * @return 성공 시 200 OK, 대상이 없을 경우 404 Not Found를 반환하는 ResponseEntity
     */
    @PutMapping("/{budgetId}")
    public ResponseEntity<Void> updateBudget(@PathVariable Long budgetId, @RequestBody BudgetDTO budgetDTO) {
        log.info("▶▶▶ [API Start] updateBudget | budgetId: {}", budgetId);
        try {
            String userId = getCurrentUserId();
            boolean success = budgetService.updateBudget(userId, budgetId, budgetDTO);
            if (success) {
                log.info("예산 수정 성공 | budgetId: {}", budgetId);
                return ResponseEntity.ok().build();
            } else {
                log.warn("수정할 예산 없음 | budgetId: {}", budgetId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("예산 수정 중 에러 발생 | budgetId: {}", budgetId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] updateBudget");
        }
    }

    /**
     * 특정 예산을 삭제하는 API.
     * @param budgetId 삭제할 예산의 ID (URL 경로 변수)
     * @return 성공 시 200 OK, 대상이 없을 경우 404 Not Found를 반환하는 ResponseEntity
     */
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long budgetId) {
        log.info("▶▶▶ [API Start] deleteBudget | budgetId: {}", budgetId);
        try {
            String userId = getCurrentUserId();
            boolean success = budgetService.deleteBudget(userId, budgetId);
            if (success) {
                log.info("예산 삭제 성공 | budgetId: {}", budgetId);
                return ResponseEntity.ok().build();
            } else {
                log.warn("삭제할 예산 없음 | budgetId: {}", budgetId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("예산 삭제 중 에러 발생 | budgetId: {}", budgetId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] deleteBudget");
        }
    }

    /**
     * 특정 사용자의 특정 연/월 예산 목록을 조회하는 API.
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 조회된 예산 목록({@link BudgetEntity})을 포함하는 ResponseEntity
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<BudgetEntity>> getBudgetsByYearMonth(@RequestParam int year, @RequestParam int month) {
        log.info("▶▶▶ [API Start] getBudgetsByYearMonth | year: {}, month: {}", year, month);
        try {
            String userId = getCurrentUserId();
            List<BudgetEntity> result = budgetService.getBudgetsByUserIdAndYearMonth(userId, year, month);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("연/월 기준 예산 조회 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] getBudgetsByYearMonth");
        }
    }

    /**
     * 현재 로그인된 사용자의 모든 예산 목록을 조회하는 API.
     * @return 조회된 예산 목록({@link BudgetEntity})을 포함하는 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<BudgetEntity>> getBudgetsForCurrentUser() {
        log.info("▶▶▶ [API Start] getBudgetsForCurrentUser");
        try {
            String userId = getCurrentUserId();
            List<BudgetEntity> budgets = budgetService.getBudgetsByUserId(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            log.error("사용자 전체 예산 조회 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] getBudgetsForCurrentUser");
        }
    }

    /**
     * 예산 ID를 통해 특정 예산 단건을 조회하는 API.
     * @param budgetId 조회할 예산의 ID
     * @return 조회된 예산 정보({@link BudgetDTO})를 포함하는 ResponseEntity
     */
    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable Long budgetId) {
        log.info("▶▶▶ [API Start] getBudgetById | budgetId: {}", budgetId);
        try {
            String userId = getCurrentUserId();
            BudgetDTO budget = budgetService.getBudgetById(userId, budgetId);
            if (budget == null) {
                log.warn("조회할 예산 없음 | budgetId: {}", budgetId);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            log.error("ID 기준 예산 조회 중 에러 발생 | budgetId: {}", budgetId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] getBudgetById");
        }
    }

    /**
     * 현재 로그인된 사용자의 모든 예산을 최신 물가상승률에 따라 조정하는 API.
     * @return 조정된 예산 목록 또는 오류 응답
     */
    @PutMapping("/actions/adjust-cpi")
    public ResponseEntity<List<BudgetEntity>> adjustBudgetsForCpi() {
        log.info("▶▶▶ [API Start] adjustBudgetsForCpi");
        try {
            String userId = getCurrentUserId();
            List<BudgetEntity> adjustedBudgets = budgetService.adjustAllBudgetsForCpi(userId);
            log.info("물가상승률 기반 예산 조정 성공 | userId: {}", userId);
            return ResponseEntity.ok(adjustedBudgets);
        } catch (AccessDeniedException e) {
            log.warn("로그인하지 않은 사용자의 예산 조정 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("물가상승률 기반 예산 조정 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] adjustBudgetsForCpi");
        }
    }
}
