package kopo.newproject.controller;

import kopo.newproject.dto.GoalDTO;

import kopo.newproject.service.impl.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/goalAPI")
@RequiredArgsConstructor
public class GoalAPIController {

    private final GoalService goalService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 목표 생성
    @PostMapping
    public ResponseEntity<GoalDTO> createGoal(@RequestBody GoalDTO dto) {
        try {
            dto.setUserId(getCurrentUserId());
            GoalDTO created = goalService.saveGoal(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("목표 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 목표 전체 조회 (유저별)
    @GetMapping
    public ResponseEntity<List<GoalDTO>> getGoals() {
        try {
            List<GoalDTO> goals = goalService.getGoalsByUser(getCurrentUserId());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            log.error("목표 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 목표 단건 조회
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDTO> getGoal(@PathVariable Long goalId) {
        try {
            List<GoalDTO> goals = goalService.getGoalsByUser(getCurrentUserId());
            return goals.stream()
                    .filter(g -> g.getGoalId().equals(goalId))
                    .findFirst()
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("목표 단건 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 목표 수정
    @PutMapping("/{goalId}")
    public ResponseEntity<GoalDTO> updateGoal(@PathVariable Long goalId, @RequestBody GoalDTO dto) {
        try {
            GoalDTO updated = goalService.updateGoal(goalId, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("목표 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 목표 삭제
    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long goalId) {
        try {
            goalService.deleteGoal(goalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("목표 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
