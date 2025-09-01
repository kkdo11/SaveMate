package kopo.newproject.service.impl;

import kopo.newproject.dto.MailDTO;
import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.PasswordChangeRequest;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.service.IMailService;
import kopo.newproject.service.IUserInfoService;
import kopo.newproject.util.CmmUtil;
import kopo.newproject.util.CreatePassword;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoService implements IUserInfoService {

    //회원관련 repository
    private final UserInfoRepository userInfoRepository;


    private final MailService mailService;

    @Autowired
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserInfoDTO getUserIdExists(@NonNull UserInfoDTO pDTO) throws Exception {

        String user_id = CmmUtil.nvl(pDTO.user_id());

        //DB에서 아이디 중복 여부 확인
        boolean exists = userInfoRepository.findByUserId(user_id).isPresent();

        //존재 여부에 따라 DTO 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                .exist_yn(exists ? "Y" : "N")
                .build();

        return rDTO;
    }

    @Override
    public UserInfoDTO getEmailExists(@NonNull UserInfoDTO pDTO) throws Exception {

        String email = CmmUtil.nvl(pDTO.email());

        //DB에서 이메일 중복 여부 확인
        boolean exists = userInfoRepository.findByEmail(email).isPresent();

        //존재 여부에 따라 DTO 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                .exist_yn(exists ? "Y" : "N")
                .build();

        return rDTO;
    }

    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {

        int res; //회원가입 성공 : 1  ,  아이디 중복으로 인한 가입 취소 : 2   ,   그 외 에러 : 3

        String user_id = CmmUtil.nvl(pDTO.user_id());
        String email = CmmUtil.nvl(pDTO.email());
        String password = CmmUtil.nvl(pDTO.password());
        String name = CmmUtil.nvl(pDTO.name());
        String gender = CmmUtil.nvl(pDTO.gender()); // gender 추가
        String birthDate = CmmUtil.nvl(pDTO.birthDate()); // birthDate 추가

        //암호화된 패스워드
        String encPassword = passwordEncoder.encode(password);


        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(user_id);

        if (rEntity.isPresent()) {
            res = 2;
        } else {
            UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                    .userId(user_id).email(email)
                    .password(encPassword).name(name)
                    .gender(gender) // gender 추가
                    .birthDate(birthDate) // birthDate 추가
                    .build();

            //회원정보 DB에 저장
            userInfoRepository.save(userInfoEntity);

            //JPA의 SAVE함수는 데이터 값에 따라 등록 수정을 수행함
            //실행한 save함수가 DB에 잘 등록되었는지 100% 확신이 불가능하기에
            // 회원가입후 조회를 수행한다
            //회원가입 중복 방지를 위해 DB에서 데이터 조회
            res = userInfoRepository.findByUserId(user_id).isPresent() ? 1 : 0;
            if (res == 1) {
            } else {
            }
        }

        return res;
    }

    @Override
    public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {

        String user_id = CmmUtil.nvl(pDTO.user_id());
        String password = CmmUtil.nvl(pDTO.password());


    return 0;
    }


    //아이디 찾기
    @Override
    public UserInfoDTO findUserIdByNameAndEmail(UserInfoDTO pDTO) throws Exception {

        String name = CmmUtil.nvl(pDTO.name());
        String email = CmmUtil.nvl(pDTO.email());

        // 이름과 이메일이 일치하는 사용자 조회
        Optional<UserInfoEntity> rEntity = userInfoRepository.findByNameAndEmail(name, email);

        if (rEntity.isPresent()) {
            log.info("[UserInfoService] 아이디 찾기 성공 - user_id: {}", rEntity.get().getUserId());
            return UserInfoDTO.builder()
                    .user_id(rEntity.get().getUserId())  // user_id를 결과로 리턴
                    .build();
        }

        log.warn("[UserInfoService] 아이디 찾기 실패: 일치하는 정보 없음 - name: {}, email: {}", name, email);
        return null;
    }


    //비밀번호 찾기
    @Override
    public UserInfoDTO findPWDByIdAndEmail(UserInfoDTO pDTO) throws Exception {

        String id = CmmUtil.nvl(pDTO.user_id());
        String email = CmmUtil.nvl(pDTO.email());

        // 아이디와 이메일이 일치하는 사용자 조회
        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserIdAndEmail(id, email);

        if (rEntity.isPresent()) {
            log.info("[UserInfoService] 비밀번호 찾기 성공 - user_id: {}", rEntity.get().getUserId());
            return UserInfoDTO.builder()
                    .user_id(rEntity.get().getUserId())  // user_id를 결과로 리턴
                    .build();
        }

        log.warn("[UserInfoService] 비밀번호 찾기 실패: 일치하는 정보 없음 - userId: {}, email: {}", id, email);
        return null;
    }


    //임시 비밀번호 발급
    @Override
    public MsgDTO resetUserPassword(String user_id, String email) throws Exception {

        // 사용자 정보 조회
        Optional<UserInfoEntity> userOpt = userInfoRepository.findByUserIdAndEmail(user_id, email);

        if (userOpt.isEmpty()) {
            log.warn("[UserInfoService] 비밀번호 재발급 실패: 일치하는 사용자 없음 - userId: {}, email: {}", user_id, email);
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
        log.info("[UserInfoService] 사용자 {}의 비밀번호가 임시 비밀번호로 업데이트됨.", user_id);

        // 이메일 전송
        MailDTO mailDTO = MailDTO.builder()
                .toMail(email)
                .title("임시 비밀번호 발급 안내")
                .contents("임시 비밀번호는 <b>" + tempPassword + "</b> 입니다.<br>로그인 후 비밀번호를 꼭 변경해주세요.")
                .build();


        int mailResult = mailService.doSendMail(mailDTO);

        if (mailResult == 1) {
            log.info("[UserInfoService] 임시 비밀번호 이메일 전송 성공 - {}", email);
            return MsgDTO.builder()
                    .result(1)
                    .msg("임시 비밀번호가 이메일로 전송되었습니다.")
                    .build();
        } else {
            log.error("[UserInfoService] 임시 비밀번호 이메일 전송 실패 - {}", email);
            return MsgDTO.builder()
                    .result(0)
                    .msg("임시 비밀번호 생성은 성공했지만, 이메일 전송에 실패했습니다.")
                    .build();
        }
    }


    //비밀번호 변경
    @Override
    public boolean changePassword(String user_id, PasswordChangeRequest request) {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(user_id);
        if (optionalUser.isPresent()) {
            UserInfoEntity user = optionalUser.get();

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                log.warn("[UserInfoService] 비밀번호 변경 실패: 현재 비밀번호 불일치 - {}", user_id);
                return false; // 현재 비밀번호 불일치
            }

            user.changePassword(passwordEncoder.encode(request.getNewPassword()));
            userInfoRepository.save(user);
            log.info("[UserInfoService] 사용자 {}의 비밀번호가 성공적으로 변경됨.", user_id);
            return true;
        }
        log.warn("[UserInfoService] 비밀번호 변경 실패: 사용자 찾을 수 없음 - {}", user_id);
        return false;
    }


    @Override
    public UserInfoDTO findByUserId(String user_id) throws Exception {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(user_id);

        if (optionalUser.isPresent()) {
            UserInfoEntity entity = optionalUser.get();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAtStr = entity.getCreatedAt().format(formatter);

            UserInfoDTO rDTO = UserInfoDTO.builder()
                    .user_id(entity.getUserId())
                    .email(entity.getEmail())
                    .password(null) // 보안상 제외
                    .name(entity.getName())
                    .created_at(createdAtStr)
                    .globalAlertEnabled(entity.getGlobalAlertEnabled())
                    .gender(entity.getGender())
                    .birthDate(entity.getBirthDate())
                    .exist_yn(null)
                    .build();
            log.info("[UserInfoService] findByUserId end - userId: {}", user_id);
            return rDTO;
        } else {
            log.warn("[UserInfoService] findByUserId 실패: 사용자 찾을 수 없음 - userId: {}", user_id);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean updateGlobalAlertSetting(String userId, Boolean enabled) throws Exception {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            UserInfoEntity user = optionalUser.get();
            user.setGlobalAlertEnabled(enabled); // UserInfoEntity에 setter가 없으면 오류 발생
            userInfoRepository.save(user);
            log.info("[UserInfoService] 사용자 {}의 전역 알림 설정이 {}로 변경되었습니다.", userId, enabled);
            return true;
        }
        log.warn("[UserInfoService] updateGlobalAlertSetting 실패: 사용자 찾을 수 없음 - userId: {}", userId);
        return false;
    }

    @Override
    @Transactional
    public boolean updateAutoBudgetAdjustmentSetting(String userId, Boolean enabled) throws Exception {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            UserInfoEntity user = optionalUser.get();
            user.setAutoBudgetAdjustmentEnabled(enabled);
            userInfoRepository.save(user);
            log.info("[UserInfoService] 사용자 {}의 자동 예산 조정 설정이 {}로 변경되었습니다.", userId, enabled);
            return true;
        }
        log.warn("[UserInfoService] updateAutoBudgetAdjustmentSetting 실패: 사용자 찾을 수 없음 - userId: {}", userId);
        return false;
    }

    @Override
    @Transactional
    public boolean updateBudgetAlertThresholdSetting(String userId, Double thresholdPercentage) throws Exception {
        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            UserInfoEntity user = optionalUser.get();
            user.setBudgetAlertThresholdPercentage(thresholdPercentage);
            userInfoRepository.save(user);
            log.info("[UserInfoService] 사용자 {}의 예산 알림 임계값 설정이 {}로 변경되었습니다.", userId, thresholdPercentage);
            return true;
        }
        log.warn("[UserInfoService] updateBudgetAlertThresholdSetting 실패: 사용자 찾을 수 없음 - userId: {}", userId);
        return false;
    }

    @Override
    public List<UserInfoEntity> getAllUsers() throws Exception {
        log.info("Getting all users");
        return userInfoRepository.findAll();
    }
}









