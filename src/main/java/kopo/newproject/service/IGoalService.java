package kopo.newproject.service;

import kopo.newproject.dto.GoalDTO;

import java.util.List;

/**
 * 재정 목표(Goal) 관련 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 */
public interface IGoalService {

    /**
     * 새로운 재정 목표를 저장하거나, 이미 존재하는 경우 업데이트합니다.
     *
     * @param goalDTO 저장 또는 업데이트할 목표 정보를 담은 DTO
     * @return 저장 또는 업데이트된 목표 정보를 담은 DTO
     */
    GoalDTO saveGoal(GoalDTO goalDTO);

    /**
     * 특정 사용자의 모든 재정 목표 목록을 조회합니다.
     *
     * @param userId 목표를 조회할 사용자 ID
     * @return 해당 사용자의 재정 목표 DTO 리스트
     */
    List<GoalDTO> getGoalsByUser(String userId);

    /**
     * 기존 재정 목표의 내용을 수정합니다.
     * (중요: 구현체에서는 반드시 해당 goalId의 소유자가 현재 로그인한 사용자인지 확인해야 합니다.)
     *
     * @param goalId  수정할 목표의 고유 ID
     * @param goalDTO 수정할 내용을 담은 DTO
     * @return 수정된 목표 정보를 담은 DTO
     */
    GoalDTO updateGoal(Long goalId, GoalDTO goalDTO);

    /**
     * 특정 재정 목표를 삭제합니다.
     * (중요: 구현체에서는 반드시 해당 goalId의 소유자가 현재 로그인한 사용자인지 확인해야 합니다.)
     *
     * @param goalId 삭제할 목표의 고유 ID
     */
    void deleteGoal(Long goalId);
}

