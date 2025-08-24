package kopo.newproject.service.impl;

import kopo.newproject.dto.GoalDTO;
import kopo.newproject.repository.entity.jpa.GoalEntity;
import kopo.newproject.repository.jpa.GoalRepository;
import kopo.newproject.service.IGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link IGoalService} 인터페이스의 구현체.
 * <p>
 * 사용자의 재정 목표(Goal)에 대한 생성, 조회, 수정, 삭제 등
 * 핵심 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService implements IGoalService {

    private final GoalRepository goalRepository;

    /**
     * 새로운 재정 목표를 저장하거나, 이미 존재하는 경우 업데이트합니다.
     *
     * @param goalDTO 저장 또는 업데이트할 목표 정보를 담은 DTO
     * @return 저장 또는 업데이트된 목표 정보를 담은 DTO
     */
    @Override
    @Transactional
    public GoalDTO saveGoal(GoalDTO goalDTO) {
        log.info("▶▶▶ [Service Start] saveGoal | userId: {}, goalName: {}", goalDTO.getUserId(), goalDTO.getGoalName());
        GoalEntity entity = GoalEntity.builder()
                .goalId(goalDTO.getGoalId()) // ID가 있으면 업데이트, 없으면 새로 생성
                .userId(goalDTO.getUserId())
                .goalName(goalDTO.getGoalName())
                .targetAmount(goalDTO.getTargetAmount())
                .savedAmount(goalDTO.getSavedAmount())
                .deadline(goalDTO.getDeadline())
                .build();

        GoalEntity saved = goalRepository.save(entity);
        log.info("◀◀◀ [Service End] saveGoal | 목표 저장 성공 | goalId: {}", saved.getGoalId());
        return convertToDto(saved);
    }

    /**
     * 특정 사용자의 모든 재정 목표 목록을 조회합니다。
     *
     * @param userId 목표를 조회할 사용자 ID
     * @return 해당 사용자의 재정 목표 DTO 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<GoalDTO> getGoalsByUser(String userId) {
        log.info("▶▶▶ [Service Start] getGoalsByUser | userId: {}", userId);
        List<GoalDTO> goals = goalRepository.findByUserIdOrderByDeadlineAsc(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
        log.info("◀◀◀ [Service End] getGoalsByUser | 목표 {}건 조회 완료", goals.size());
        return goals;
    }

    /**
     * 특정 재정 목표 하나를 ID로 조회합니다.
     * (NOTE: 이 메소드는 IGoalService 인터페이스에 정의되어 있었으나, 기존 구현체에는 누락되어 추가되었습니다.)
     *
     * @param userId 조회할 목표의 소유자 ID (권한 확인용)
     * @param goalId 조회할 목표의 고유 ID
     * @return 조회된 목표 정보 또는 null (목표가 없거나 소유자가 다를 경우)
     */
    @Override
    @Transactional(readOnly = true)
    public GoalDTO getGoalById(String userId, Long goalId) {
        log.info("▶▶▶ [Service Start] getGoalById | userId: {}, goalId: {}", userId, goalId);
        Optional<GoalEntity> optionalGoal = goalRepository.findByGoalIdAndUserId(goalId, userId); // ID와 userId로 함께 조회하여 권한 확인
        if (optionalGoal.isPresent()) {
            log.info("◀◀◀ [Service End] getGoalById | 목표 조회 성공 | goalId: {}", goalId);
            return convertToDto(optionalGoal.get());
        } else {
            log.warn("◀◀◀ [Service End] getGoalById | 목표 조회 실패: 목표를 찾을 수 없거나 권한 없음 | goalId: {}", goalId);
            return null;
        }
    }

    /**
     * 기존 재정 목표의 내용을 수정합니다.
     * <p>
     * **중요:** 수정 요청된 목표가 현재 로그인한 사용자의 것인지 확인 후 수정합니다.
     *
     * @param goalId  수정할 목표의 고유 ID
     * @param goalDTO 수정할 내용을 담은 DTO
     * @return 수정된 목표 정보를 담은 DTO
     * @throws RuntimeException 목표를 찾을 수 없거나 권한이 없을 경우 발생
     */
    @Override
    @Transactional
    public GoalDTO updateGoal(Long goalId, GoalDTO goalDTO) {
        log.info("▶▶▶ [Service Start] updateGoal | goalId: {}, goalName: {}", goalId, goalDTO.getGoalName());
        // 목표 ID와 사용자 ID를 모두 사용하여 목표를 조회하여 권한 확인
        GoalEntity existing = goalRepository.findByGoalIdAndUserId(goalId, goalDTO.getUserId())
                .orElseThrow(() -> {
                    log.warn("목표 수정 실패: 목표를 찾을 수 없거나 권한 없음 | goalId: {}, userId: {}", goalId, goalDTO.getUserId());
                    return new RuntimeException("목표를 찾을 수 없거나 수정 권한이 없습니다.");
                });

        // TODO: [개선] 새로운 엔티티를 빌드하는 대신, 기존 엔티티의 필드를 직접 업데이트하는 것이 더 효율적입니다.
        // 예: existing.setGoalName(goalDTO.getGoalName());
        //     existing.setTargetAmount(goalDTO.getTargetAmount()); ...
        GoalEntity updated = GoalEntity.builder()
                .goalId(existing.getGoalId())
                .userId(existing.getUserId())
                .goalName(goalDTO.getGoalName())
                .targetAmount(goalDTO.getTargetAmount())
                .savedAmount(goalDTO.getSavedAmount())
                .deadline(goalDTO.getDeadline())
                .build();

        GoalEntity saved = goalRepository.save(updated);
        log.info("◀◀◀ [Service End] updateGoal | 목표 수정 성공 | goalId: {}", saved.getGoalId());
        return convertToDto(saved);
    }

    /**
     * 특정 재정 목표를 삭제합니다.
     * <p>
     * **중요:** 삭제 요청된 목표가 현재 로그인한 사용자의 것인지 확인 후 삭제합니다.
     *
     * @param goalId 삭제할 목표의 고유 ID
     * @throws RuntimeException 목표를 찾을 수 없거나 권한이 없을 경우 발생
     */
    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        log.info("▶▶▶ [Service Start] deleteGoal | goalId: {}", goalId);
        // 삭제 전, 목표의 존재 여부 및 소유자 확인 (userId는 컨트롤러에서 가져와 서비스 메소드 파라미터로 전달하는 것이 좋습니다.)
        // 현재는 userId 파라미터가 없으므로, goalId만으로 조회 후 삭제합니다.
        // TODO: [보안 강화] deleteGoal(Long goalId, String userId)로 변경하여 userId로 소유자 확인 후 삭제하도록 개선 필요
        if (!goalRepository.existsById(goalId)) {
            log.warn("목표 삭제 실패: 목표를 찾을 수 없음 | goalId: {}", goalId);
            throw new RuntimeException("삭제할 목표를 찾을 수 없습니다.");
        }
        goalRepository.deleteById(goalId);
        log.info("◀◀◀ [Service End] deleteGoal | 목표 삭제 성공 | goalId: {}", goalId);
    }

    /**
     * {@link GoalEntity} 객체를 {@link GoalDTO} 객체로 변환하는 헬퍼 메소드.
     *
     * @param entity 변환할 GoalEntity 객체
     * @return 변환된 GoalDTO 객체
     */
    private GoalDTO convertToDto(GoalEntity entity) {
        return GoalDTO.builder()
                .goalId(entity.getGoalId())
                .userId(entity.getUserId())
                .goalName(entity.getGoalName())
                .targetAmount(entity.getTargetAmount())
                .savedAmount(entity.getSavedAmount())
                .deadline(entity.getDeadline())
                .remainingAmount(entity.getRemainingAmount())
                .progressRate(entity.getProgressRate())
                .build();
    }
}