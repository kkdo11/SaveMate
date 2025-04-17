package kopo.newproject.service.impl;




import kopo.newproject.dto.MailDTO;
import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.PasswordChangeRequest;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.service.IUserInfoService;
import kopo.newproject.util.CmmUtil;
import kopo.newproject.util.CreatePassword;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
//@RequiredArgsConstructor는 초기화 되지않은 final 필드나,
// @NonNull 이 붙은 필드에 대해 생성자를 생성
@Service
public class UserInfoService implements IUserInfoService {
    
    //회원관련 repository
    private final UserInfoRepository userInfoRepository;
    private final MailService mailService;

    @Autowired
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserInfoDTO getUserIdExists(@NonNull  UserInfoDTO pDTO) throws Exception {
        log.info("getUserIdExists start", this.getClass().getName());

        log.info("userInfoDTO: {}", pDTO);

        String user_id = CmmUtil.nvl(pDTO.user_id());
        
        //DB에서 아이디 중복 여부 확인
        boolean exists = userInfoRepository.findByUserId(user_id).isPresent();
        
        //존재 여부에 따라 DTO 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                        .exist_yn(exists ? "Y": "N")
                        .build();
        log.info("userInfoDTO: {}", rDTO);

        log.info("getUserIdExists end", this.getClass().getName());

        return rDTO;
    }
    @Override
    public UserInfoDTO getEmailExists(@NonNull  UserInfoDTO pDTO) throws Exception {
        log.info("{}.getEmailExists start", this.getClass().getName());

        log.info("userInfoDTO: {}", pDTO);

        String email = CmmUtil.nvl(pDTO.email());

        //DB에서 이메일 중복 여부 확인
        boolean exists = userInfoRepository.findByEmail(email).isPresent();

        //존재 여부에 따라 DTO 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                .exist_yn(exists ? "Y": "N")
                .build();

        log.info("userInfoDTO: {}", rDTO);

        log.info("{}.getEmailExists end", this.getClass().getName());

        return rDTO;
    }

    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {
        log.info("insertUserInfo start", this.getClass().getName());

        log.info("userInfoDTO: {}", pDTO);

        int res; //회원가입 성공 : 1  ,  아이디 중복으로 인한 가입 취소 : 2   ,   그 외 에러 : 3

        String user_id = CmmUtil.nvl(pDTO.user_id());
        String email = CmmUtil.nvl(pDTO.email());
        String password = CmmUtil.nvl(pDTO.password());
        String name = CmmUtil.nvl(pDTO.name());

        //암호화된 패스워드
        String encPassword = passwordEncoder.encode(password);


        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(user_id);

        if (rEntity.isPresent()) {
            res=2;
        }else {
            UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                    .userId(user_id).email(email)
                    .password(encPassword).name(name)
                    .build();

            //회원정보 DB에 저장
            userInfoRepository.save(userInfoEntity);
            
            //JPA의 SAVE함수는 데이터 값에 따라 등록 수정을 수행함
            //실행한 save함수가 DB에 잘 등록되었는지 100% 확신이 불가능하기에
            // 회원가입후 조회를 수행한다
            //회원가입 중복 방지를 위해 DB에서 데이터 조회
            res = userInfoRepository.findByUserId(user_id).isPresent() ? 1 : 0;
        }
        
        log.info("insertUserInfo end", this.getClass().getName());
        return res;
    }

/*    //로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기

    @Override
    public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("getUserLoginCheck start", this.getClass().getName());

        String user_id = CmmUtil.nvl(pDTO.user_id());
        String password = CmmUtil.nvl(pDTO.password());

        log.info("user_id : {},password : {} ",user_id,password);

        boolean res =  userInfoRepository.findByUserIdAndPassword(user_id,password).isPresent();

        log.info("getUserLoginCheck end", this.getClass().getName());

        return res ? 1 : 0;



    }*/
@Override
public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {
    log.info("getUserLoginCheck start", this.getClass().getName());

    String user_id = CmmUtil.nvl(pDTO.user_id());
    String password = CmmUtil.nvl(pDTO.password());

    log.info("user_id : {}, password : {}", user_id, password);

    Optional<UserInfoEntity> userOpt = userInfoRepository.findByUserId(user_id);

    if (userOpt.isPresent()) {
        String encPassword = userOpt.get().getPassword();
        if (passwordEncoder.matches(password, encPassword)) {
            log.info("비밀번호 일치");
            return 1;
        } else {
            log.info("비밀번호 불일치");
            return 0;
        }
    }

    log.info("아이디 없음");
    return 0;
}


    //아이디 찾기
    @Override
    public UserInfoDTO findUserIdByNameAndEmail(UserInfoDTO pDTO) throws Exception {
        log.info("{}.findUserIdByNameAndEmail start", this.getClass().getName());

        String name = CmmUtil.nvl(pDTO.name());
        String email = CmmUtil.nvl(pDTO.email());

        // 이름과 이메일이 일치하는 사용자 조회
        Optional<UserInfoEntity> rDTO = userInfoRepository.findByNameAndEmail(name, email);

        if (rDTO.isPresent()) {
            log.info("아이디 조회 성공: {}", rDTO.get().getUserId());
            return UserInfoDTO.builder()
                    .user_id(rDTO.get().getUserId())  // user_id를 결과로 리턴
                    .build();
        }

        log.info("일치하는 사용자가 존재하지 않음.");
        return null;
    }






    @Override
    public MsgDTO resetUserPassword(String name, String email) throws Exception {
        log.info("비밀번호 재발급 요청: name={}, email={}", name, email);

        // 사용자 정보 조회
        Optional<UserInfoEntity> userOpt = userInfoRepository.findByNameAndEmail(name, email);

        if (userOpt.isEmpty()) {
            return MsgDTO.builder()
                    .result(0)
                    .msg("입력한 정보와 일치하는 사용자가 없습니다.")
                    .build();
        }

        UserInfoEntity user = userOpt.get();

        // 임시 비밀번호 생성 및 암호화
        String tempPassword = CreatePassword.createTempPassword();
        String encPassword = passwordEncoder.encode(tempPassword);

        // 비밀번호 업데이트
        user.changePassword(encPassword);
        userInfoRepository.save(user);

        // 이메일 전송
        MailDTO mailDTO = MailDTO.builder()
                .toMail(email)
                .title("임시 비밀번호 발급 안내")
                .contents("임시 비밀번호는 <b>" + tempPassword + "</b> 입니다.<br>로그인 후 비밀번호를 꼭 변경해주세요.")
                .build();


        int mailResult = mailService.doSendMail(mailDTO);

        if (mailResult == 1) {
            return MsgDTO.builder()
                    .result(1)
                    .msg("임시 비밀번호가 이메일로 전송되었습니다.")
                    .build();
        } else {
            return MsgDTO.builder()
                    .result(0)
                    .msg("임시 비밀번호 생성은 성공했지만, 이메일 전송에 실패했습니다.")
                    .build();
        }
    }

    public UserInfoEntity getUserInfo(String user_id) {
        return userInfoRepository.findByUserId(user_id)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
    }

    @Override
    public boolean changePassword(String user_id, PasswordChangeRequest request) {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(user_id);
        if (optionalUser.isPresent()) {
            UserInfoEntity user = optionalUser.get();

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return false; // 현재 비밀번호 불일치
            }

            user.changePassword(passwordEncoder.encode(request.getNewPassword()));
            userInfoRepository.save(user);
            return true;
        }
        return false;
    }


    @Override
    public UserInfoDTO findByUserId(String user_id) throws Exception {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(user_id);

        if (optionalUser.isPresent()) {
            UserInfoEntity entity = optionalUser.get();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAtStr = entity.getCreatedAt().format(formatter);

            return UserInfoDTO.builder()
                    .user_id(entity.getUserId())
                    .email(entity.getEmail())
                    .password(null) // 보안상 제외
                    .name(entity.getName())
                    .created_at(createdAtStr)
                    .exist_yn(null)
                    .build();
        } else {
            return null;
        }
    }


}










