    package kopo.newproject.service.impl;

    import kopo.newproject.dto.GoalDTO;
    import kopo.newproject.repository.entity.jpa.GoalEntity;
    import kopo.newproject.repository.jpa.GoalRepository;
    import kopo.newproject.service.IGoalService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class GoalService implements IGoalService {

        private final GoalRepository goalRepository;

        // 목표 저장
        public GoalDTO saveGoal(GoalDTO dto) {
            GoalEntity entity = GoalEntity.builder()
                    .userId(dto.getUserId())
                    .goalName(dto.getGoalName())
                    .targetAmount(dto.getTargetAmount())
                    .savedAmount(dto.getSavedAmount())
                    .deadline(dto.getDeadline())
                    .build();

            GoalEntity saved = goalRepository.save(entity);

            return convertToDto(saved);
        }

        // 목표 조회
        public List<GoalDTO> getGoalsByUser(String userId) {
            return goalRepository.findByUserIdOrderByDeadlineAsc(userId)
                    .stream()
                    .map(this::convertToDto)
                    .toList();
        }

        // 목표 수정
        // 목표 수정
        public GoalDTO updateGoal(Long goalId, GoalDTO dto) {
            GoalEntity existing = goalRepository.findById(goalId)
                    .orElseThrow(() -> new RuntimeException("목표 없음"));

            // 기존 데이터를 바탕으로 새 Entity 생성
            GoalEntity updated = GoalEntity.builder()
                    .goalId(existing.getGoalId())        // 기존 PK 유지
                    .userId(existing.getUserId())        // 기존 userId 유지
                    .goalName(dto.getGoalName())         // 변경 내용 반영
                    .targetAmount(dto.getTargetAmount())
                    .savedAmount(dto.getSavedAmount())
                    .deadline(dto.getDeadline())
                    .build();

            return convertToDto(goalRepository.save(updated));
        }


        // 목표 삭제
        public void deleteGoal(Long goalId) {
            goalRepository.deleteById(goalId);
        }

        // 내부 변환 메서드
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
