package kopo.newproject.service;

import kopo.newproject.dto.GoalDTO;

import java.util.List;

public interface IGoalService {

    GoalDTO saveGoal(GoalDTO dto);

    List<GoalDTO> getGoalsByUser(String userId);

    GoalDTO updateGoal(Long goalId, GoalDTO dto);

    void deleteGoal(Long goalId);
}
