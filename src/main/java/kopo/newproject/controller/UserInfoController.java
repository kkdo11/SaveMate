package kopo.newproject.controller;

import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.service.IUserInfoService;
import kopo.newproject.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


import java.util.Optional;

@Slf4j
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Controller
public class UserInfoController {

    private final IUserInfoService userInfoService;


    @GetMapping(value = "userRegForm")
    public String userRegForm() {
        log.info("user/userRegForm start" , this.getClass().getName());

        log.info("user/userRegForm end" , this.getClass().getName());
        return "user/userRegForm";
    }

    //회원가입 전 아이디 중복 체크하기(Ajax 를 통해 아이디 정보 받음)
    @ResponseBody
    @PostMapping(value = "getUserIdExists")
    public UserInfoDTO getUserExists(HttpServletRequest request) throws Exception {

        log.info("{}.getUserIdExists start" ,this.getClass().getName());//회원아이디

        String userId = CmmUtil.nvl(request.getParameter("user_id"));

        log.info("userid : {}", userId);

        // Builder 를 통한 값 저장
        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        // 회원아이디를 통해 중복된 아이디 인지 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getUserIdExists end" , this.getClass().getName());

        return rDTO;
    }

    //회원가입 로직 처리
    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insetUserInfo(HttpServletRequest request) throws Exception{
        log.info("{}.insertUserInfo start" ,this.getClass().getName());

        String msg;

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String email = CmmUtil.nvl(request.getParameter("email"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String name = CmmUtil.nvl(request.getParameter("name"));

        log.info("userid : {} , email : {} , password : {} , name : {}", userId, email, password, name);


        UserInfoDTO pDTO = UserInfoDTO.builder().
                userId(userId)
                .email(email)
                .password(password)
                .name(name)
                .build();

        int res = userInfoService.insertUserInfo(pDTO);

        log.info("회원가입 결과(res) : {}", res);


        if (res ==1 ) {
            msg = "회원가입 되었습니다";
        } else if (res ==2) {
            msg="이미 가입된 아이디 입니다";
        }else {
            msg="오류로 인해 회원가입이 실패하였습니다";
        }


        log.info("{}.insertUserInfo end" , this.getClass().getName());

        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();

        return dto;
    }

    //로그인을 위한 입력 화면으로 이동
    @GetMapping(value = "login")
    public String login() {
        log.info("login start" , this.getClass().getName());

        log.info("login end" , this.getClass().getName());

        return "user/login";
    }

    //로그인 처리 및 결과를 알려주는 화면으로 이동
    @ResponseBody
    @PostMapping(value = "loginProc")
    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) throws Exception {
        log.info("loginProc start" , this.getClass().getName());

        String msg; //로그인 결과에 대한 메시지를 전달할 변수

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String password = CmmUtil.nvl(request.getParameter("password"));

        log.info("user_id : {} , password : {}", userId, password);

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).password(password).build();

        int res = userInfoService.getUserLogin(pDTO);

        log.info("res : {}", res);

        if (res ==1 ) {
            msg = "로그인이 성공했습니다";
            session.setAttribute("SS_USER_ID", userId);
        }else {
            msg = "아이디와 비밀번호가 올바르지 않습니다";
        }

        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();

        log.info("{}.loginProc end" , this.getClass().getName());

        return dto;
    }

    //로그인 성공 페이지 이동
    @GetMapping(value = "loginsuccess")
    public String loginSuccess() {
        log.info("loginSuccess start" , this.getClass().getName());

        log.info("loginSuccess end" , this.getClass().getName());

        return "user/loginSuccess";
    }

    //로그아웃 처리하기
    @ResponseBody
    @PostMapping(value = "logout")
    public MsgDTO logout(HttpSession session) throws Exception {
        log.info("logout start" , this.getClass().getName());

        session.setAttribute("SS_USER_ID", "");
        session.removeAttribute("SS_USER_ID");

        MsgDTO dto = MsgDTO.builder().result(1).msg("로그아웃 하였습니다").build();

        log.info("{}.logoutSuccess end" , this.getClass().getName());

        return dto;
    }

}
