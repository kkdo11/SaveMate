package kopo.newproject.controller;

import kopo.newproject.dto.GoalDTO;
import kopo.newproject.service.impl.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 재정 목표(Goal) CRUD 관련 API 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @RestController} - 이 컨트롤러의 모든 메소드는 JSON 형태의 데이터를 반환합니다.
 * {@code @RequestMapping("/api/goals")} - RESTful 원칙에 따라 리소스 중심으로 경로를 설정합니다.
 * {@code @RequiredArgsConstructor} - final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/goals") // RESTful API 경로 규칙에 따라 /goalAPI -> /api/goals 로 수정
@RequiredArgsConstructor
public class GoalAPIController {

    private final GoalService goalService;

    /**
     * Spring Security 컨텍스트에서 현재 인증된 사용자의 ID를 안전하게 가져오는 헬퍼 메소드.
     * @return 현재 로그인된 사용자의 ID
     */
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 새로운 재정 목표를 생성하는 API.
     * @param goalDTO HTTP 요청 Body에 담겨온 목표 생성 정보 (JSON)
     * @return 생성된 목표 정보({@link GoalDTO})를 포함하는 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<GoalDTO> createGoal(@RequestBody GoalDTO goalDTO) {
        log.info("▶▶▶ [API Start] createGoal");
        try {
            goalDTO.setUserId(getCurrentUserId());
            GoalDTO created = goalService.saveGoal(goalDTO);
            log.info("목표 생성 성공 | goalId: {}", created.getGoalId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("목표 생성 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] createGoal");
        }
    }

    /**
     * 현재 로그인된 사용자의 모든 재정 목표를 조회하는 API.
     * @return 목표 목록({@link List<GoalDTO>})을 포함하는 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<GoalDTO>> getAllGoalsForUser() {
        log.info("▶▶▶ [API Start] getAllGoalsForUser");
        try {
            List<GoalDTO> goals = goalService.getGoalsByUser(getCurrentUserId());
            log.info("사용자 목표 {}건 조회 성공", goals.size());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            log.error("사용자 목표 조회 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] getAllGoalsForUser");
        }
    }

    /**
     * 특정 재정 목표 하나를 ID로 조회하는 API.
     * @param goalId 조회할 목표의 ID (URL 경로 변수)
     * @return 조회된 목표 정보 또는 404 Not Found 응답
     */
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDTO> getGoalById(@PathVariable Long goalId) {
        log.info("▶▶▶ [API Start] getGoalById | goalId: {}", goalId);
        try {
            // TODO: [Refactoring] 현재 로직은 사용자의 모든 목표를 가져와 필터링하므로 비효율적입니다.
            //  서비스 계층에 'getGoalByIdAndUserId(Long goalId, String userId)'와 같은 메소드를 만들어
            //  DB에서 직접 단일 목표를 조회하도록 개선하는 것이 좋습니다.
            List<GoalDTO> goals = goalService.getGoalsByUser(getCurrentUserId());
            return goals.stream()
                    .filter(g -> g.getGoalId().equals(goalId))
                    .findFirst()
                    .map(goal -> {
                        log.info("ID 기준 목표 조회 성공 | goalId: {}", goalId);
                        return ResponseEntity.ok(goal);
                    })
                    .orElseGet(() -> {
                        log.warn("조회할 목표 없음 | goalId: {}", goalId);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("ID 기준 목표 조회 중 에러 발생 | goalId: {}", goalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] getGoalById");
        }
    }

    /**
     * 기존 재정 목표를 수정하는 API.
     * @param goalId  수정할 목표의 ID (URL 경로 변수)
     * @param goalDTO 수정할 목표 정보 (JSON)
     * @return 수정된 목표 정보를 포함하는 ResponseEntity
     */
    @PutMapping("/{goalId}")
    public ResponseEntity<GoalDTO> updateGoal(@PathVariable Long goalId, @RequestBody GoalDTO goalDTO) {
        log.info("▶▶▶ [API Start] updateGoal | goalId: {}", goalId);
        try {
            // 중요: 서비스 계층(goalService.updateGoal)에서
            //  해당 goalId의 소유자가 현재 로그인한 사용자인지 반드시 검증해야 합니다.
            GoalDTO updated = goalService.updateGoal(goalId, goalDTO);
            log.info("목표 수정 성공 | goalId: {}", goalId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            // TODO: AccessDeniedException 등 특정 예외를 잡아 403 Forbidden 또는 404 Not Found 반환 처리 필요
            log.error("목표 수정 중 에러 발생 | goalId: {}", goalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] updateGoal");
        }
    }

    /**
     * 특정 재정 목표를 삭제하는 API.
     * @param goalId 삭제할 목표의 ID (URL 경로 변수)
     * @return 성공 시 200 OK 응답
     */
    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long goalId) {
        log.info("▶▶▶ [API Start] deleteGoal | goalId: {}", goalId);
        try {
            // 중요: 서비스 계층(goalService.deleteGoal)에서
            //  해당 goalId의 소유자가 현재 로그인한 사용자인지 반드시 검증해야 합니다.
            goalService.deleteGoal(goalId);
            log.info("목표 삭제 성공 | goalId: {}", goalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // TODO: AccessDeniedException 등 특정 예외를 잡아 403 Forbidden 또는 404 Not Found 반환 처리 필요
            log.error("목표 삭제 중 에러 발생 | goalId: {}", goalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            log.info("◀◀◀ [API End] deleteGoal");
        }
    }
}
