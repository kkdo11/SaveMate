package kopo.newproject.service;

import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.PasswordChangeRequest;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;

import java.util.List;

/**
 * 사용자 정보 관련 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 * 회원가입, 정보 조회/수정, 아이디/비밀번호 찾기, 설정 변경 등의 기능을 포함합니다.
 */
public interface IUserInfoService {

    /**
     * 회원가입을 위해 사용자 아이디(ID)가 이미 존재하는지 중복 체크를 합니다.
     * @param pDTO 확인할 사용자 ID를 담은 DTO
     * @return 존재 여부(exist_yn)가 포함된 DTO
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;


    /**
     * 회원가입을 위해 이메일 주소가 이미 존재하는지 중복 체크를 합니다.
     * @param pDTO 확인할 이메일 주소를 담은 DTO
     * @return 존재 여부(exist_yn)가 포함된 DTO
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

    /**
     * 사용자의 이름과 이메일 주소를 기반으로 아이디를 찾습니다.
     * @param pDTO 찾기 위해 필요한 이름과 이메일 주소를 담은 DTO
     * @return 조회된 사용자 정보를 담은 DTO. 없을 경우 null.
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    UserInfoDTO findUserIdByNameAndEmail(UserInfoDTO pDTO) throws Exception;

    /**
     * 임시 비밀번호를 발급하기 위해 아이디와 이메일 주소로 사용자를 찾습니다.
     * @param pDTO 찾기 위해 필요한 아이디와 이메일 주소를 담은 DTO
     * @return 조회된 사용자 정보를 담은 DTO. 없을 경우 null.
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    UserInfoDTO findPWDByIdAndEmail(UserInfoDTO pDTO) throws Exception;

    /**
     * 사용자의 비밀번호를 임시 비밀번호로 재설정하고, 이메일로 발송합니다.
     * @param userId 재설정할 사용자의 ID
     * @param email  재설정할 사용자의 이메일
     * @return 처리 결과(성공/실패)와 메시지를 담은 DTO
     * @throws Exception 데이터 처리 및 이메일 발송 중 예외 발생 시
     */
    MsgDTO resetUserPassword(String userId, String email) throws Exception;

    /**
     * 새로운 사용자 정보를 등록합니다. (회원가입)
     * @param pDTO 회원가입할 사용자 정보를 담은 DTO
     * @return 성공: 1, 아이디 중복: 2, 기타 오류: 0
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;


    /**
     * 사용자 ID를 기준으로 사용자 정보를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 정보를 담은 DTO. 없을 경우 null.
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    UserInfoDTO findByUserId(String userId) throws Exception;

    /**
     * (NOTE: 이 메소드는 Spring Security의 인증 메커니즘과 중복될 수 있습니다.)
     * 로그인을 위해 아이디와 비밀번호가 일치하는지 확인합니다.
     * 일반적으로 이러한 로직은 Spring Security의 AuthenticationProvider에서 처리하는 것이 권장됩니다.
     * @param pDTO 확인할 아이디와 비밀번호를 담은 DTO
     * @return 일치하면 1, 불일치하면 0
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    int getUserLogin(UserInfoDTO pDTO) throws Exception;


    /**
     * 사용자의 현재 비밀번호를 확인하고 새로운 비밀번호로 변경합니다.
     * @param userId          비밀번호를 변경할 사용자 ID
     * @param request         현재 비밀번호와 새로운 비밀번호를 담은 DTO
     * @return 성공 여부
     */
    boolean changePassword(String userId, PasswordChangeRequest request);

    /**
     * 사용자의 전역 알림(소비 초과 경고 등) 수신 여부 설정을 업데이트합니다.
     * @param userId  설정을 변경할 사용자 ID
     * @param enabled 활성화 여부
     * @return 성공 여부
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    boolean updateGlobalAlertSetting(String userId, Boolean enabled) throws Exception;

    /**
     * 사용자의 예산 자동 조정(물가상승률 연동) 기능 활성화 여부를 업데이트합니다.
     * @param userId  설정을 변경할 사용자 ID
     * @param enabled 활성화 여부
     * @return 성공 여부
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    boolean updateAutoBudgetAdjustmentSetting(String userId, Boolean enabled) throws Exception;

    /**
     * ❗️(주의)❗️ 모든 사용자 목록을 조회합니다.
     * 개인정보를 포함할 수 있으므로, 관리자 기능 등 매우 제한적인 경우에만 사용해야 합니다.
     * @return 전체 사용자 엔티티 목록
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    List<UserInfoEntity> getAllUsers() throws Exception;
}