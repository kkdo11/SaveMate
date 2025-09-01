package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

    // 사용자별 목표 리스트
    List<GoalEntity> findByUserId(String userId);

    // 사용자별 월별 마감일 기준 정렬
    List<GoalEntity> findByUserIdOrderByDeadlineAsc(String userId);
}
