package kopo.newproject.repository.mongo;

import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // LocalDateTime 임포트 추가
import java.util.List;
import java.util.Optional;

/**
 * {@link AIAnalysisEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data MongoDB의 {@link MongoRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface AIAnalysisRepository extends MongoRepository<AIAnalysisEntity, String> {

    /**
     * 특정 사용자 ID와 연월에 해당하는 모든 AI 분석 결과를 조회합니다.
     *
     * @param userId  조회할 사용자의 ID
     * @param month   조회할 연월 (예: "2025-08")
     * @return 조건에 맞는 AI 분석 엔티티 목록
     */
    List<AIAnalysisEntity> findByUserIdAndMonth(String userId, String month);

    /**
     * 특정 사용자 ID와 연월에 해당하는 모든 AI 분석 결과를 삭제합니다.
     *
     * @param userId  삭제할 사용자의 ID
     * @param month   삭제할 연월 (예: "2025-08")
     */
    void deleteByUserIdAndMonth(String userId, String month);

    /**
     * 특정 사용자의 AI 분석 결과 중 가장 최근에 생성된 단일 결과를 조회합니다.
     * 생성일시(createdAt)를 기준으로 내림차순 정렬하여 가장 첫 번째 결과를 가져옵니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 가장 최근의 AI 분석 엔티티 (Optional)
     */
    Optional<AIAnalysisEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 특정 사용자 ID와 연월에 해당하는 모든 AI 분석 결과를 생성일시(createdAt)를 기준으로
     * 내림차순 정렬하여 조회합니다. (가장 최신 결과가 먼저 오도록)
     *
     * @param userId  조회할 사용자의 ID
     * @param month   조회할 연월 (예: "2025-08")
     * @return 조건에 맞는 AI 분석 엔티티 목록 (최신순)
     */
    List<AIAnalysisEntity> findByUserIdAndMonthOrderByCreatedAtDesc(String userId, String month);

    /**
     * 특정 분석 ID와 사용자 ID를 사용하여 AI 분석 결과를 조회합니다.
     * <p>
     * 이 메소드는 분석 결과의 존재 여부와 함께 해당 분석이 특정 사용자에게 속하는지 확인하는 데 사용됩니다.
     * 주로 분석 결과 수정 또는 삭제 시 권한 확인을 위해 호출됩니다.
     *
     * @param id     조회할 분석 결과의 고유 ID
     * @param userId 분석 결과의 소유자 사용자 ID
     * @return 조건에 맞는 AI 분석 엔티티 (Optional)
     */
    Optional<AIAnalysisEntity> findByIdAndUserId(String id, String userId);

    /**
     * 특정 사용자 ID와 연월, 그리고 생성일시 범위 내에 해당하는 AI 분석 결과의 개수를 조회합니다.
     * <p>
     * (NOTE: {@code java.util.Date} 대신 {@code java.time.LocalDateTime}을 사용하는 것이
     * 최신 Java API 사용 및 일관성 측면에서 권장됩니다.)
     *
     * @param userId 조회할 사용자의 ID
     * @param month  조회할 연월 (예: "2025-08")
     * @param start  생성일시 시작 범위 (포함)
     * @param end    생성일시 종료 범위 (포함)
     * @return 조건에 맞는 AI 분석 결과의 개수
     */
    long countByUserIdAndMonthAndCreatedAtBetween(String userId, String month, java.util.Date start, java.util.Date end);
}