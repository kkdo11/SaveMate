package kopo.newproject.repository;

import kopo.newproject.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {
    //회원의 존재 여부를 체크한다
    //Optional 객체는 객체에 값이 존재하는지 확인할떄에 사용한다
    Optional<UserInfoEntity> findByUserId(String userId);

    //로그인
    // 쿼리 예 : select * from USER_INFO where USER_ID = ' ** ' and password = '1234'
    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);
}
