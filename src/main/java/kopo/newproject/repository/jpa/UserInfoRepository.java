package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {
    //회원의 존재 여부를 체크한다
    //Optional 객체는 객체에 값이 존재하는지 확인할떄에 사용한다
    Optional<UserInfoEntity> findByUserId(String user_id);

    // 이메일의 존재 여부 체크
    Optional<UserInfoEntity> findByEmail(String email);


    Optional<UserInfoEntity> findByNameAndEmail(String name, String email);





}

