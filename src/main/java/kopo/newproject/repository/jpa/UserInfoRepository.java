package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link UserInfoEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    /**
     * 특정 사용자 ID를 사용하여 사용자 정보를 조회합니다.
     * <p>
     * 주로 사용자 존재 여부 확인, 로그인 처리, 사용자 정보 조회 등에 사용됩니다.
     * {@link Optional} 객체로 반환되어 null 처리의 안정성을 높입니다.
     *
     * @param userId 조회할 사용자의 고유 ID
     * @return 조건에 맞는 사용자 엔티티 (Optional)
     */
    Optional<UserInfoEntity> findByUserId(String userId);

    /**
     * 특정 이메일 주소를 사용하여 사용자 정보를 조회합니다.
     * <p>
     * 주로 이메일 중복 확인, 비밀번호 찾기 등 이메일 기반의 사용자 조회에 사용됩니다.
     *
     * @param email 조회할 사용자의 이메일 주소
     * @return 조건에 맞는 사용자 엔티티 (Optional)
     */
    Optional<UserInfoEntity> findByEmail(String email);

    /**
     * 사용자의 이름과 이메일 주소를 사용하여 사용자 정보를 조회합니다.
     * <p>
     * 주로 아이디 찾기 기능에서 사용됩니다.
     *
     * @param name  조회할 사용자의 이름
     * @param email 조회할 사용자의 이메일 주소
     * @return 조건에 맞는 사용자 엔티티 (Optional)
     */
    Optional<UserInfoEntity> findByNameAndEmail(String name, String email);

    /**
     * 사용자의 아이디와 이메일 주소를 사용하여 사용자 정보를 조회합니다.
     * <p>
     * 주로 비밀번호 찾기 기능에서 사용됩니다.
     *
     * @param userId 조회할 사용자의 아이디
     * @param email  조회할 사용자의 이메일 주소
     * @return 조건에 맞는 사용자 엔티티 (Optional)
     */
    Optional<UserInfoEntity> findByUserIdAndEmail(String userId, String email);
}