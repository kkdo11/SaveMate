package kopo.newproject.repository.mongo;

import kopo.newproject.dto.SpendingTotalDTO;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * {@link SpendingEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data MongoDB의 {@link MongoRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface SpendingRepository extends MongoRepository<SpendingEntity, ObjectId> {

    /**
     * 특정 사용자 ID와 카테고리에 해당하는 지출 내역 목록을 조회합니다.
     *
     * @param userId   조회할 사용자의 ID
     * @param category 조회할 카테고리
     * @return 조건에 맞는 지출 엔티티 목록
     */
    List<SpendingEntity> findByUserIdAndCategory(String userId, String category);

    /**
     * 특정 사용자 ID에 해당하는 모든 지출 내역 목록을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 모든 지출 엔티티 목록
     */
    List<SpendingEntity> findByUserId(String userId);

    /**
     * 특정 사용자 ID와 날짜 범위에 해당하는 지출 금액의 총합을 계산합니다.
     * <p>
     * MongoDB 애그리게이션 파이프라인을 사용하여 데이터를 집계합니다.
     *
     * @param userId    조회할 사용자의 ID
     * @param startDate 지출 시작일 (포함)
     * @param endDate   지출 종료일 (미포함)
     * @return 총 지출 금액을 담은 {@link SpendingTotalDTO}. 지출이 없으면 total 필드가 null일 수 있습니다.
     */
    @Aggregation(pipeline = {
            "{$match: { 'userId': ?0, 'date': { $gte: ?1, $lt: ?2 } }}", // 사용자 ID와 날짜 범위로 필터링
            "{$group: { '_id': null, 'total': { $sum: '$amount' } }}" // 필터링된 문서들의 'amount' 필드를 합산
    })
    SpendingTotalDTO sumAmountByDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 사용자 ID, 날짜 범위, 그리고 카테고리에 해당하는 지출 금액의 총합을 계산합니다.
     * <p>
     * MongoDB 애그리게이션 파이프라인을 사용하여 데이터를 집계합니다.
     *
     * @param userId    조회할 사용자의 ID
     * @param startDate 지출 시작일 (포함)
     * @param endDate   지출 종료일 (미포함)
     * @param category  조회할 카테고리
     * @return 총 지출 금액을 담은 {@link SpendingTotalDTO}. 지출이 없으면 total 필드가 null일 수 있습니다.
     */
    @Aggregation(pipeline = {
            "{$match: { 'userId': ?0, 'date': { $gte: ?1, $lt: ?2 }, 'category': ?3 }}", // 사용자 ID, 날짜 범위, 카테고리로 필터링
            "{$group: { '_id': null, 'total': { $sum: '$amount' } }}" // 필터링된 문서들의 'amount' 필드를 합산
    })
    SpendingTotalDTO sumAmountByDateBetweenAndCategory(String userId, LocalDate startDate, LocalDate endDate, String category);

    /**
     * 특정 사용자 ID와 날짜 범위에 해당하는 지출 내역 목록을 조회합니다.
     *
     * @param userId    조회할 사용자의 ID
     * @param startDate 지출 시작일 (포함)
     * @param endDate   지출 종료일 (미포함)
     * @return 조건에 맞는 지출 엔티티 목록
     */
    List<SpendingEntity> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 사용자 ID, 날짜 범위, 그리고 카테고리에 해당하는 지출 내역 목록을 조회합니다.
     *
     * @param userId    조회할 사용자의 ID
     * @param startDate 지출 시작일 (포함)
     * @param endDate   지출 종료일 (미포함)
     * @param category  조회할 카테고리
     * @return 조건에 맞는 지출 엔티티 목록
     */
    List<SpendingEntity> findByUserIdAndDateBetweenAndCategory(String userId, LocalDate startDate, LocalDate endDate, String category);
}
