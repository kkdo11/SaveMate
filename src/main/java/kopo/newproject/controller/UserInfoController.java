package kopo.newproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kopo.newproject.dto.MailDTO;
import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.PasswordChangeRequest;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.service.IMailService;
import kopo.newproject.service.IUserInfoService;
import kopo.newproject.service.IBudgetService;
import kopo.newproject.repository.entity.jpa.BudgetEntity; // BudgetEntity 임포트
import kopo.newproject.util.CmmUtil;
import java.util.List; // List 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // HttpStatus 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserInfoController {

    private static final Logger log = LoggerFactory.getLogger(UserInfoController.class);

    private final IUserInfoService userInfoService;
    private final IMailService mailService;
    private final IBudgetService budgetService; // IBudgetService 주입

    /** ------------------------ 페이지 이동 ------------------------ **/

    @GetMapping("/userRegForm")
    public String userRegForm() {
        log.info("[userRegForm] 회원가입 페이지 이동 요청");
        return "user/userRegForm";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        log.info("[loginPage] 로그인 페이지 이동 요청, error: {}", error);
        if (error != null) {
            model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return "user/login";
    }


    @GetMapping("/findID")
    public String findIDPage() {
        log.info("[findIDPage] 아이디 찾기 페이지 이동 요청");
        return "user/findID";
    }

    @GetMapping("/findPWD")
    public String findPWDPage() {
        log.info("[findPWDPage] 비밀번호 찾기 페이지 이동 요청");
        return "user/findPWD";
    }






    /** ------------------------ 회원가입 ------------------------ **/

    @ResponseBody
    @PostMapping("/insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) throws Exception {
        log.info("[insertUserInfo] 회원가입 요청 시작");

        String user_id = CmmUtil.nvl(request.getParameter("user_id"));
        String email = CmmUtil.nvl(request.getParameter("email"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String name = CmmUtil.nvl(request.getParameter("name"));
        String gender = CmmUtil.nvl(request.getParameter("gender")); // gender 추가
        String birthDate = CmmUtil.nvl(request.getParameter("birthDate")); // birthDate 추가

        log.info("입력 데이터 - user_id: {}, email: {}, name: {}, gender: {}, birthDate: {}", user_id, email, name, gender, birthDate);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .user_id(user_id)
                .email(email)
                .password(password)
                .name(name)
                .gender(gender)
                .birthDate(birthDate)
                .build();

        int res = userInfoService.insertUserInfo(pDTO);
        String msg;

        if (res == 1) {
            msg = "회원가입 되었습니다";
            log.info("회원가입 성공, 이메일 발송 시작");
            mailService.doSendMail(MailDTO.builder()
                    .toMail(email)
                    .title("SaveMate 회원가입 완료")
                    .contents("SaveMate에 정상적으로 회원가입이 완료되었습니다.")
                    .build());
        } else if (res == 2) {
            msg = "이미 가입된 아이디입니다.";
            log.info("회원가입 실패 - 아이디 중복");
        } else {
            msg = "회원가입 실패. 관리자에게 문의하세요.";
            log.warn("회원가입 실패 - 기타 사유");


        }

        log.info("[insertUserInfo] 회원가입 결과: {}", msg);
        return MsgDTO.builder().result(res).msg(msg).build();
    }

    /** ------------------------ 중복 확인 ------------------------ **/

    @ResponseBody
    @GetMapping("/getUserIdExists")
    public UserInfoDTO getUserIdExists(HttpServletRequest request) {
        log.info("[getUserIdExists] 아이디 중복 확인 요청 시작");

        String user_id = CmmUtil.nvl(request.getParameter("user_id"));
        log.info("입력된 user_id: {}", user_id);

        try {
            // Service에서 반환한 exist_yn 값을 그대로 전달
            UserInfoDTO rDTO = userInfoService.getUserIdExists(
                    UserInfoDTO.builder().user_id(user_id).build());

            log.info("서비스 결과 exist_yn: {}", rDTO.exist_yn());
            return rDTO;

        } catch (Exception e) {
            log.error("아이디 중복 확인 중 예외 발생: {}", e.getMessage(), e);
            return UserInfoDTO.builder()
                    .exist_yn("Y") // 예외 시 기본값 설정
                    .build();
        }
    }




    @ResponseBody
    @PostMapping("/getEmailExists")
    public UserInfoDTO getEmailExists(HttpServletRequest request) {
        log.info("[getEmailExists] 이메일 중복 확인 요청 시작");

        String email = CmmUtil.nvl(request.getParameter("email"));
        log.info("입력된 email: {}", email);

        try {
            UserInfoDTO rDTO = userInfoService.getEmailExists(
                    UserInfoDTO.builder().email(email).build());
            log.info("조회 결과: {}", rDTO != null ? "이메일 존재함" : "이메일 없음");

            return Optional.ofNullable(rDTO).orElse(UserInfoDTO.builder().build());

        } catch (Exception e) {
            log.error("이메일 중복 확인 중 예외 발생: {}", e.getMessage(), e);
            return UserInfoDTO.builder().build();
        }
    }

    /** ------------------------ 이메일 인증 ------------------------ **/

    @ResponseBody
    @PostMapping("/sendVerificationEmail")
    public MsgDTO sendVerificationEmail(HttpServletRequest request, HttpSession session) throws Exception {
        log.info("[sendVerificationEmail] 인증 메일 전송 요청 시작");

        String email = CmmUtil.nvl(request.getParameter("email"));
        log.info("입력된 email: {}", email);

        String code = mailService.generateVerificationCode();
        int res = mailService.sendVerificationMail(email, code);

        log.info("이메일 인증코드 : {}",code);

        if (res == 1) {
            session.setAttribute("emailToVerify", email);
            session.setAttribute("emailVerificationCode", code);
            log.info("인증코드 저장 완료 - email: {}, code: {}", email, code);
        } else {
            log.warn("인증 메일 전송 실패");
        }

        String msg = (res == 1) ? "인증 메일이 전송되었습니다." : "이메일을 확인하세요.";
        return MsgDTO.builder().result(res).msg(msg).build();
    }

    @ResponseBody
    @PostMapping("/verifyEmailCode")
    public MsgDTO verifyEmailCode(HttpServletRequest request, HttpSession session) {
        log.info("[verifyEmailCode] 이메일 인증 코드 확인 요청 시작");

        String email = CmmUtil.nvl(request.getParameter("email"));
        String inputCode = CmmUtil.nvl(request.getParameter("code"));

        String sessionEmail = (String) session.getAttribute("emailToVerify");
        String sessionCode = (String) session.getAttribute("emailVerificationCode");

        log.info("[입력값] email: {}, code: {}", email, inputCode);
        log.info("[세션값] emailToVerify: {}, emailVerificationCode: {}", sessionEmail, sessionCode);

        if (sessionEmail == null || sessionCode == null) {
            log.warn("이메일 인증 실패 - 세션에 저장된 인증 정보 없음");
            return MsgDTO.builder()
                    .result(0)
                    .msg("인증 세션이 만료되었거나 존재하지 않습니다. 다시 요청해주세요.")
                    .build();
        }

        if (email.equals(sessionEmail) && inputCode.equals(sessionCode)) {
            session.removeAttribute("emailToVerify");
            session.removeAttribute("emailVerificationCode");

            log.info("이메일 인증 성공 - 세션 정보 제거 완료");
            return MsgDTO.builder()
                    .result(1)
                    .msg("이메일 인증에 성공하였습니다.")
                    .build();
        } else {
            log.warn("이메일 인증 실패 - 입력값과 세션값 불일치");
            return MsgDTO.builder()
                    .result(0)
                    .msg("인증 코드가 일치하지 않습니다.")
                    .build();
        }
    }


    /** ------------------------ 아이디/비밀번호 찾기 ------------------------ **/


    @ResponseBody
    @PostMapping("/findUserId")
    public MsgDTO findUserId(HttpServletRequest request) {
        log.info("[findUserId] 아이디 찾기 요청 시작");


        try {
            String name = CmmUtil.nvl(request.getParameter("name"));
            String email = CmmUtil.nvl(request.getParameter("email"));
            log.info("입력 name: {}, email: {}", name, email);

            UserInfoDTO rDTO = userInfoService.findUserIdByNameAndEmail(
                    UserInfoDTO.builder().name(name).email(email).build());

            if (rDTO != null && rDTO.user_id() != null) {
                log.info("아이디 찾기 성공 - user_id: {}", rDTO.user_id());
                return MsgDTO.builder().result(1).msg("회원님의 아이디는 [" + rDTO.user_id() + "]입니다.").build();
            } else {
                log.warn("아이디 찾기 실패 - 일치하는 정보 없음");
                return MsgDTO.builder().result(0).msg("일치하는 아이디가 없습니다.").build();
            }

        } catch (Exception e) {
            log.error("아이디 찾기 중 예외 발생: {}", e.getMessage(), e);
            return MsgDTO.builder().result(0).msg("서버 오류 발생. 관리자에게 문의하세요.").build();
        }
    }

    @ResponseBody
    @PostMapping("/findPwd")
    public MsgDTO findPWD(HttpServletRequest request) {
        log.info("[findPwd] 요청 시작");


        try {
            String id = CmmUtil.nvl(request.getParameter("user_id"));
            String email = CmmUtil.nvl(request.getParameter("email"));
            log.info("입력 user_id: {}, email: {}", id, email);

            UserInfoDTO rDTO = userInfoService.findPWDByIdAndEmail(
                    UserInfoDTO.builder().user_id(id).email(email).build());

            if (rDTO != null && rDTO.user_id() != null) {
                log.info("findPwd 성공 - user_id: {}", rDTO.user_id());
                return MsgDTO.builder().result(1).msg("회원님의 아이디는 [" + rDTO.user_id() + "]입니다.").build();
            } else {
                log.warn("findPwd 실패 - 일치하는 정보 없음");
                return MsgDTO.builder().result(0).msg("일치하는 정보 없습니다.").build();
            }

        } catch (Exception e) {
            log.error("아이디 찾기 중 예외 발생: {}", e.getMessage(), e);
            return MsgDTO.builder().result(0).msg("서버 오류 발생. 관리자에게 문의하세요.").build();
        }
    }


    //임시 비밀번호 발송 및 비밀번호 업데이트
    @ResponseBody
    @PostMapping("/resetPassword")
    public ResponseEntity<MsgDTO> resetPassword(@RequestParam String user_id,
                                                @RequestParam String email) throws Exception {
        log.info("[resetPassword] 비밀번호 재설정 요청 - user_id: {}, email: {}", user_id, email);

        MsgDTO dto = userInfoService.resetUserPassword(user_id, email);

        if (dto.result() == 1) {
            log.info("비밀번호 재설정 성공");
            return ResponseEntity.ok(dto);
        } else {
            log.warn("비밀번호 재설정 실패 - {}", dto.msg());
            return ResponseEntity.badRequest().body(dto);
        }
    }


    @GetMapping("/myPage")
    public String myPage(Model model, @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        if (userDetails == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        String username = userDetails.getUsername();  // 로그인한 사용자 ID
        // username을 통해 사용자 정보 조회
        UserInfoDTO userInfo = userInfoService.findByUserId(username); // 예시 메서드
        model.addAttribute("userInfo", userInfo);

        // ✅ 현재 사용자의 예산 목록을 가져와 모델에 추가
        List<BudgetEntity> budgets = budgetService.getBudgetsByUserId(username);
        model.addAttribute("budgets", budgets);
        log.info("마이페이지에 추가된 예산 수: {}", budgets.size());

        return "user/myPage";
    }



@PostMapping("/changePassword")
public String changePassword(@RequestParam String currentPassword,
                             @RequestParam String newPassword,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             RedirectAttributes redirectAttributes) {

    if (userDetails == null) {
        redirectAttributes.addFlashAttribute("errorMessage", "로그인된 사용자가 없습니다.");
        return "redirect:/user/myPage";
    }

    PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(currentPassword, newPassword);
    boolean result = userInfoService.changePassword(userDetails.getUsername(), passwordChangeRequest);

    if (result) {
        log.info("비밀번호 변경됨");
        // ✅ 세션 종료 및 로그아웃
        new SecurityContextLogoutHandler().logout(request, response, null);

        // ✅ login.html에서 메시지를 감지할 수 있도록 URL 파라미터 추가
        return "redirect:/user/login?passwordChanged=true";
    } else {
        log.info("비밀번호 변경 실패");
        redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 올바르지 않거나 사용자 정보를 찾을 수 없습니다.");
        return "redirect:/user/myPage";
    }
}


    // ✅ 전역 알림 설정 업데이트 API
    @PutMapping("/global-alert-setting")
    public ResponseEntity<Void> updateGlobalAlertSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Boolean enabled
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = userDetails.getUsername();
        try {
            userInfoService.updateGlobalAlertSetting(userId, enabled);
            log.info("사용자 {}의 전역 알림 설정이 {}로 변경되었습니다.", userId, enabled);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("사용자 {}의 전역 알림 설정 변경 중 오류 발생: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ 자동 예산 조정 설정 업데이트 API
    @PutMapping("/auto-budget-adjustment-setting")
    public ResponseEntity<Void> updateAutoBudgetAdjustmentSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Boolean enabled
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = userDetails.getUsername();
        try {
            userInfoService.updateAutoBudgetAdjustmentSetting(userId, enabled);
            log.info("사용자 {}의 자동 예산 조정 설정이 {}로 변경되었습니다.", userId, enabled);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("사용자 {}의 자동 예산 조정 설정 변경 중 오류 발생: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ 예산 알림 임계값 설정 업데이트 API
    @PutMapping("/budget-alert-threshold-setting")
    public ResponseEntity<Void> updateBudgetAlertThresholdSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Double thresholdPercentage
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = userDetails.getUsername();
        try {
            userInfoService.updateBudgetAlertThresholdSetting(userId, thresholdPercentage);
            log.info("사용자 {}의 예산 알림 임계값 설정이 {}%로 변경되었습니다.", userId, thresholdPercentage * 100);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("사용자 {}의 예산 알림 임계값 설정 변경 중 오류 발생: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }








}
