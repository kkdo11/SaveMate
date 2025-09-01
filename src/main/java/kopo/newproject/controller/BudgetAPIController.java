package kopo.newproject.controller;

import kopo.newproject.dto.BudgetDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.service.IBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;

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
            BudgetEntity created = budgetService.createBudget(userId, dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
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
            boolean success = budgetService.updateBudget(userId, budgetId, dto);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
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
            boolean success = budgetService.deleteBudget(userId, budgetId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
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
            BudgetEntity result = budgetService.getBudgetByUserIdAndYearMonth(userId, year, month);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
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
            List<BudgetEntity> result;
            if (month == null && (category == null || category.isBlank())) {
                result = budgetService.findAll();
            } else {
                result = budgetService.findByMonthAndCategory(month, category);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    // 유저별 전체 예산 목록 조회
    @GetMapping
    public ResponseEntity<List<BudgetEntity>> getBudgetsByUserId() {
        try {
            String userId = getCurrentUserId();
            List<BudgetEntity> budgets = budgetService.getBudgetsByUserId(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 월별 전체 예산(카테고리별) 조회
    @GetMapping("/monthly")
    public ResponseEntity<List<BudgetEntity>> getBudgetsByMonth(
            @RequestParam("month") String monthStr
    ) {
        try {
            String userId = getCurrentUserId();
            // monthStr: "YYYY-MM" 형식
            String[] parts = monthStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            List<BudgetEntity> budgets = budgetService.getBudgetsByUserIdAndYearMonth(userId, year, month);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 로그인된 사용자의 모든 예산을 최신 물가상승률에 따라 조정합니다.
     * @return 조정된 예산 목록 또는 오류 응답
     */
    @PutMapping("/actions/adjust-cpi")
    public ResponseEntity<List<BudgetEntity>> adjustBudgetsForCpi() {
        try {
            String userId = getCurrentUserId();
            List<BudgetEntity> adjustedBudgets = budgetService.adjustAllBudgetsForCpi(userId);
            return ResponseEntity.ok(adjustedBudgets);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
