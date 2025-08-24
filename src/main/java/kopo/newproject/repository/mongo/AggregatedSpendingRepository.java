package kopo.newproject.repository.mongo;

import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * {@link AggregatedSpendingEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data MongoDB의 {@link MongoRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface AggregatedSpendingRepository extends MongoRepository<AggregatedSpendingEntity, String> {

    /**
     * 특정 연월, 성별, 연령대 그룹에 해당하는 집계된 소비 데이터를 조회합니다.
     * <p>
     * 이 메소드는 Spring Data MongoDB의 쿼리 메소드 파생(Query Method Derivation) 기능을 사용하여
     * 메소드 이름만으로 MongoDB 쿼리를 자동으로 생성합니다.
     *
     * @param month    조회할 연월
     * @param gender   조회할 성별 ("M" 또는 "F")
     * @param ageGroup 조회할 연령대 ("10대", "20대" 등)
     * @return 조건에 맞는 집계된 소비 엔티티 (Optional)
     */
    Optional<AggregatedSpendingEntity> findByMonthAndGenderAndAgeGroup(YearMonth month, String gender, String ageGroup);

    /**
     * 특정 연월에 해당하는 모든 집계된 소비 데이터를 조회합니다.
     * <p>
     * 이 메소드는 해당 월의 모든 성별 및 연령대 그룹에 대한 집계 데이터를 반환합니다.
     *
     * @param month 조회할 연월
     * @return 조건에 맞는 집계된 소비 엔티티 목록
     */
    List<AggregatedSpendingEntity> findByMonth(YearMonth month);
}