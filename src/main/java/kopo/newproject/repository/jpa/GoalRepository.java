package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link GoalEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

    /**
     * 특정 사용자의 모든 재정 목표 목록을 조회합니다.
     *
     * @param userId 목표를 조회할 사용자 ID
     * @return 해당 사용자의 모든 재정 목표 엔티티 목록
     */
    List<GoalEntity> findByUserId(String userId);

    /**
     * 특정 사용자의 모든 재정 목표 목록을 마감일(deadline)을 기준으로 오름차순 정렬하여 조회합니다.
     *
     * @param userId 목표를 조회할 사용자 ID
     * @return 해당 사용자의 재정 목표 엔티티 목록 (마감일 오름차순)
     */
    List<GoalEntity> findByUserIdOrderByDeadlineAsc(String userId);

    /**
     * 특정 목표 ID와 사용자 ID를 사용하여 재정 목표를 조회합니다.
     * <p>
     * 이 메소드는 목표의 존재 여부와 함께 해당 목표가 특정 사용자에게 속하는지 확인하는 데 사용됩니다.
     * 주로 목표 수정 또는 삭제 시 권한 확인을 위해 호출됩니다.
     *
     * @param goalId 목표의 고유 ID
     * @param userId 목표의 소유자 사용자 ID
     * @return 조건에 맞는 재정 목표 엔티티 (Optional)
     */
    Optional<GoalEntity> findByGoalIdAndUserId(Long goalId, String userId);
}