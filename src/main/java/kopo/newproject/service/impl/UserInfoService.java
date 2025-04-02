package kopo.newproject.service.impl;




import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.UserInfoRepository;
import kopo.newproject.repository.entity.UserInfoEntity;
import kopo.newproject.service.IUserInfoService;
import kopo.newproject.util.CmmUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
//@RequiredArgsConstructor는 초기화 되지않은 final 필드나,
// @NonNull 이 붙은 필드에 대해 생성자를 생성
@Service
public class UserInfoService implements IUserInfoService {
    
    //회원관련 repository
    private final UserInfoRepository userInfoRepository;
    
    
    @Override
    public UserInfoDTO getUserIdExists(@NonNull  UserInfoDTO pDTO) throws Exception {
        log.info("getUserIdExists start", this.getClass().getName());

        log.info("userInfoDTO: {}", pDTO);

        String userId = CmmUtil.nvl(pDTO.userId());
        
        //DB에서 아이디 중복 여부 확인
        boolean exists = userInfoRepository.findByUserId(userId).isPresent();
        
        //존재 여부에 따라 DTO 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                        .exist_yn(exists ? "Y": "N")
                        .build();

        log.info("getUserIdExists end", this.getClass().getName());

        return rDTO;
    }

    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {
        log.info("insertUserInfo start", this.getClass().getName());

        log.info("userInfoDTO: {}", pDTO);

        int res; //회원가입 성공 : 1  ,  아이디 중복으로 인한 가입 취소 : 2   ,   그 외 에러 : 3

        String userId = CmmUtil.nvl(pDTO.userId());
        String email = CmmUtil.nvl(pDTO.email());
        String password = CmmUtil.nvl(pDTO.password());
        String name = CmmUtil.nvl(pDTO.name());


        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            res=2;
        }else {
            UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                    .userId(userId).email(email)
                    .password(password).name(name)
                    .build();

            //회원정보 DB에 저장
            userInfoRepository.save(userInfoEntity);
            
            //JPA의 SAVE함수는 데이터 값에 따라 등록 수정을 수행함
            //실항한 save함수가 DB에 잘 등록되었는지 100% 확신이 불가능하기에 
            // 회원가입후 조회를 수행한더
            //회원가입 중복 방지를 위해 DB에서 데이터 조회
            res = userInfoRepository.findByUserId(userId).isPresent() ? 1 : 0;
        }
        
        log.info("insertUserInfo end", this.getClass().getName());
        return res;
    }

    //로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기

    @Override
    public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {
        
        log.info("getUserLoginCheck start", this.getClass().getName());
        
        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());
        
        log.info("user_id : {},password : {} ",userId,password);
                
        boolean res =  userInfoRepository.findByUserIdAndPassword(userId,password).isPresent();
                
        log.info("getUserLoginCheck end", this.getClass().getName());
        
        return res ? 1 : 0;
    }

   



}
