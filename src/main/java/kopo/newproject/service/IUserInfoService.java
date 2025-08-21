package kopo.newproject.service;

import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.PasswordChangeRequest;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;

import java.util.List;

public interface IUserInfoService {

    //아이디 중복체크하기
    //@param userInfoDTO 회원가입을 위한 아이디
    //@return 아이디 중복 여부 결과
    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;


    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

    //아이디 찾기
    UserInfoDTO findUserIdByNameAndEmail(UserInfoDTO pDTO) throws Exception;

    UserInfoDTO findPWDByIdAndEmail(UserInfoDTO pDTO) throws Exception;

    //비밀번호 찾기
    MsgDTO resetUserPassword(String name, String email) throws Exception;

    //회원정보 등록(회원가입)
    //@return 회원가입 결과
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;


    UserInfoDTO findByUserId(String user_id) throws Exception;

    //로그인을 위해 아이디와 비빌먼호가 일치하는지 확인
    int getUserLogin(UserInfoDTO pDTO) throws Exception;


    boolean changePassword(String user_id, PasswordChangeRequest request);

    boolean updateGlobalAlertSetting(String userId, Boolean enabled) throws Exception;

    boolean updateAutoBudgetAdjustmentSetting(String userId, Boolean enabled) throws Exception;

    List<UserInfoEntity> getAllUsers() throws Exception;
}