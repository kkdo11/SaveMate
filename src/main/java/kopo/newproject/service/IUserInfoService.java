package kopo.newproject.service;

import kopo.newproject.dto.UserInfoDTO;

public interface IUserInfoService {

    //아이디 중복체크하기
    //@param userInfoDTO 회원가입을 위한 아이디
    //@return 아이디 중복 여부 결과
    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    //회원정보 등록(회원가입)
    //@return 회원가입 결과
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    //로그인을 위해 아이디와 비빌먼호가 일치하는지 확인
    int getUserLogin(UserInfoDTO pDTO) throws Exception;
}
