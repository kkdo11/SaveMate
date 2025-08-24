package kopo.newproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kopo.newproject.dto.MailDTO;
import kopo.newproject.dto.MsgDTO;
import kopo.newproject.dto.PasswordChangeRequest;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.service.IBudgetService;
import kopo.newproject.service.IMailService;
import kopo.newproject.service.IUserInfoService;
import kopo.newproject.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 정보와 관련된 모든 요청(페이지 이동, 회원가입, 로그인, 정보 수정 등)을 처리하는 컨트롤러.
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserInfoController {

    private final IUserInfoService userInfoService;
    private final IMailService mailService;
    private final IBudgetService budgetService;

    // ------------------------ 페이지 이동 ------------------------ //

    /**
     * 회원가입 폼 페이지로 이동합니다.
     * @return 회원가입 페이지 뷰 이름
     */
    @GetMapping("/userRegForm")
    public String userRegForm() {
        log.info("▶▶▶ [View] 회원가입 페이지 요청");
        return "user/userRegForm";
    }

    /**
     * 로그인 폼 페이지로 이동합니다.
     * @param error 로그인 실패 시, SecurityConfig에 의해 전달되는 에러 파라미터
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return 로그인 페이지 뷰 이름
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        log.info("▶▶▶ [View] 로그인 페이지 요청 | error: {}", error);
        if (error != null) {
            model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return "user/login";
    }

    /**
     * 아이디 찾기 페이지로 이동합니다.
     * @return 아이디 찾기 페이지 뷰 이름
     */
    @GetMapping("/findID")
    public String findIDPage() {
        log.info("▶▶▶ [View] 아이디 찾기 페이지 요청");
        return "user/findID";
    }

    /**
     * 비밀번호 찾기 페이지로 이동합니다.
     * @return 비밀번호 찾기 페이지 뷰 이름
     */
    @GetMapping("/findPWD")
    public String findPWDPage() {
        log.info("▶▶▶ [View] 비밀번호 찾기 페이지 요청");
        return "user/findPWD";
    }

    /**
     * 마이페이지로 이동합니다.
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @param userDetails 현재 로그인된 사용자의 상세 정보
     * @return 마이페이지 뷰 이름
     */
    @GetMapping("/myPage")
    public String myPage(Model model, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        log.info("▶▶▶ [View] 마이페이지 요청");
        if (userDetails == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }
        String userId = userDetails.getUsername();
        UserInfoDTO userInfo = userInfoService.findByUserId(userId);
        List<BudgetEntity> budgets = budgetService.getBudgetsByUserId(userId);

        model.addAttribute("userInfo", userInfo);
        model.addAttribute("budgets", budgets);
        log.info("마이페이지 데이터 준비 완료 | userId: {}, budgetCount: {}", userId, budgets.size());

        return "user/myPage";
    }

    // ------------------------ 회원가입 및 중복 확인 ------------------------ //

    /**
     * 회원가입 처리를 수행하는 API.
     * @param request 회원가입 폼 데이터
     * @return 처리 결과(성공:1, 아이디중복:2, 기타실패:0)와 메시지를 담은 DTO
     */
    @ResponseBody
    @PostMapping("/insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) throws Exception {
        log.info("▶▶▶ [Proc] 회원가입 처리 시작");
        // NOTE: HttpServletRequest를 직접 사용하는 대신, @RequestBody와 DTO를 사용하는 것이 더 현대적이고 안전한 방법입니다.
        String userId = CmmUtil.nvl(request.getParameter("user_id"));
        String email = CmmUtil.nvl(request.getParameter("email"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String name = CmmUtil.nvl(request.getParameter("name"));
        String gender = CmmUtil.nvl(request.getParameter("gender"));
        String birthDate = CmmUtil.nvl(request.getParameter("birthDate"));

        UserInfoDTO pDTO = UserInfoDTO.builder().user_id(userId).email(email).password(password).name(name).gender(gender).birthDate(birthDate).build();
        int res = userInfoService.insertUserInfo(pDTO);
        String msg = switch (res) {
            case 1 -> "회원가입 되었습니다";
            case 2 -> "이미 가입된 아이디입니다.";
            default -> "회원가입 실패. 관리자에게 문의하세요.";
        };

        if (res == 1) {
            mailService.doSendMail(MailDTO.builder().toMail(email).title("SaveMate 회원가입 완료").contents("SaveMate에 정상적으로 회원가입이 완료되었습니다.").build());
        }
        log.info("◀◀◀ [Proc] 회원가입 처리 완료 | 결과: {}", msg);
        return MsgDTO.builder().result(res).msg(msg).build();
    }

    /**
     * 아이디 중복 여부를 확인하는 API.
     * @param request 확인할 아이디 정보
     * @return 확인 결과(exist_yn: Y/N)를 담은 DTO
     */
    @ResponseBody
    @GetMapping("/getUserIdExists")
    public UserInfoDTO getUserIdExists(HttpServletRequest request) {
        log.info("▶▶▶ [Proc] 아이디 중복 확인 시작");
        String userId = CmmUtil.nvl(request.getParameter("user_id"));
        UserInfoDTO rDTO = userInfoService.getUserIdExists(UserInfoDTO.builder().user_id(userId).build());
        log.info("◀◀◀ [Proc] 아이디 중복 확인 완료 | 결과: {}", rDTO.exist_yn());
        return rDTO;
    }

    /**
     * 이메일 중복 여부를 확인하는 API.
     * @param request 확인할 이메일 정보
     * @return 확인 결과(exist_yn: Y/N)를 담은 DTO
     */
    @ResponseBody
    @PostMapping("/getEmailExists")
    public UserInfoDTO getEmailExists(HttpServletRequest request) {
        log.info("▶▶▶ [Proc] 이메일 중복 확인 시작");
        String email = CmmUtil.nvl(request.getParameter("email"));
        UserInfoDTO rDTO = userInfoService.getEmailExists(UserInfoDTO.builder().email(email).build());
        log.info("◀◀◀ [Proc] 이메일 중복 확인 완료");
        return Optional.ofNullable(rDTO).orElseGet(() -> UserInfoDTO.builder().build());
    }

    // ------------------------ 이메일 인증 ------------------------ //

    /**
     * 인증번호가 담긴 이메일을 발송하는 API.
     * @param request 인증할 이메일 주소
     * @param session 인증번호를 저장하기 위한 세션 객체
     * @return 처리 결과와 메시지를 담은 DTO
     */
    @ResponseBody
    @PostMapping("/sendVerificationEmail")
    public MsgDTO sendVerificationEmail(HttpServletRequest request, HttpSession session) throws Exception {
        log.info("▶▶▶ [Proc] 인증 이메일 발송 시작");
        String email = CmmUtil.nvl(request.getParameter("email"));
        String code = mailService.generateVerificationCode();
        int res = mailService.sendVerificationMail(email, code);
        if (res == 1) {
            session.setAttribute("emailVerificationCode", code);
            log.info("인증코드 세션에 저장 완료 | code: {}", code);
        }
        String msg = (res == 1) ? "인증 메일이 전송되었습니다." : "메일 발송에 실패했습니다.";
        log.info("◀◀◀ [Proc] 인증 이메일 발송 완료 | 결과: {}", res);
        return MsgDTO.builder().result(res).msg(msg).build();
    }

    /**
     * 사용자가 입력한 인증번호를 검증하는 API.
     * @param request 사용자가 입력한 인증번호
     * @param session 서버에 저장된 인증번호를 가져오기 위한 세션
     * @return 인증 성공 여부와 메시지를 담은 DTO
     */
    @ResponseBody
    @PostMapping("/verifyEmailCode")
    public MsgDTO verifyEmailCode(HttpServletRequest request, HttpSession session) {
        log.info("▶▶▶ [Proc] 이메일 인증 코드 검증 시작");
        String inputCode = CmmUtil.nvl(request.getParameter("code"));
        String sessionCode = (String) session.getAttribute("emailVerificationCode");

        if (sessionCode != null && sessionCode.equals(inputCode)) {
            session.removeAttribute("emailVerificationCode");
            log.info("이메일 인증 성공");
            return MsgDTO.builder().result(1).msg("이메일 인증에 성공하였습니다.").build();
        } else {
            log.warn("이메일 인증 실패 - 코드 불일치");
            return MsgDTO.builder().result(0).msg("인증 코드가 일치하지 않습니다.").build();
        }
    }

    // ------------------------ 아이디/비밀번호 찾기 ------------------------ //

    /**
     * 이름과 이메일로 아이디를 찾는 API.
     * @param request 이름, 이메일 정보
     * @return 찾은 아이디 또는 실패 메시지를 담은 DTO
     */
    @ResponseBody
    @PostMapping("/findUserId")
    public MsgDTO findUserId(HttpServletRequest request) {
        log.info("▶▶▶ [Proc] 아이디 찾기 시작");
        String name = CmmUtil.nvl(request.getParameter("name"));
        String email = CmmUtil.nvl(request.getParameter("email"));
        UserInfoDTO rDTO = userInfoService.findUserIdByNameAndEmail(UserInfoDTO.builder().name(name).email(email).build());
        if (rDTO != null && rDTO.user_id() != null) {
            return MsgDTO.builder().result(1).msg("회원님의 아이디는 [" + rDTO.user_id() + "]입니다.").build();
        } else {
            return MsgDTO.builder().result(0).msg("일치하는 아이디가 없습니다.").build();
        }
    }

    /**
     * 임시 비밀번호를 발급하고 이메일로 전송하는 API.
     * @param user_id 아이디
     * @param email   이메일
     * @return 처리 결과와 메시지를 담은 ResponseEntity
     */
    @ResponseBody
    @PostMapping("/resetPassword")
    public ResponseEntity<MsgDTO> resetPassword(@RequestParam String user_id, @RequestParam String email) throws Exception {
        log.info("▶▶▶ [Proc] 비밀번호 재설정 시작");
        MsgDTO dto = userInfoService.resetUserPassword(user_id, email);
        if (dto.result() == 1) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.badRequest().body(dto);
        }
    }

    // ------------------------ 마이페이지 기능 ------------------------ //

    /**
     * 현재 비밀번호를 확인하고 새로운 비밀번호로 변경합니다.
     * 성공 시, 보안을 위해 강제 로그아웃 처리 후 로그인 페이지로 리다이렉트합니다.
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새로운 비밀번호
     * @param userDetails     현재 로그인된 사용자 정보
     * @param redirectAttributes 리다이렉트 시 메시지를 전달하기 위한 객체
     * @return 성공 시 로그인 페이지, 실패 시 마이페이지로 리다이렉트 경로
     */
    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 HttpServletRequest request, HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) {
        log.info("▶▶▶ [Proc] 비밀번호 변경 시작");
        boolean result = userInfoService.changePassword(userDetails.getUsername(), new PasswordChangeRequest(currentPassword, newPassword));
        if (result) {
            new SecurityContextLogoutHandler().logout(request, response, null);
            log.info("비밀번호 변경 성공 후 자동 로그아웃");
            return "redirect:/user/login?passwordChanged=true";
        } else {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치");
            redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
            return "redirect:/user/myPage";
        }
    }

    /**
     * 전역 알림 설정을 업데이트하는 API.
     * @param userDetails 현재 로그인된 사용자 정보
     * @param enabled     활성화 여부
     * @return 성공 시 200 OK, 실패 시 에러 상태를 담은 ResponseEntity
     */
    @PutMapping("/global-alert-setting")
    public ResponseEntity<Void> updateGlobalAlertSetting(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Boolean enabled) {
        log.info("▶▶▶ [API Start] updateGlobalAlertSetting | enabled: {}", enabled);
        userInfoService.updateGlobalAlertSetting(userDetails.getUsername(), enabled);
        return ResponseEntity.ok().build();
    }

    /**
     * 자동 예산 조정 설정을 업데이트하는 API.
     * @param userDetails 현재 로그인된 사용자 정보
     * @param enabled     활성화 여부
     * @return 성공 시 200 OK, 실패 시 에러 상태를 담은 ResponseEntity
     */
    @PutMapping("/auto-budget-adjustment-setting")
    public ResponseEntity<Void> updateAutoBudgetAdjustmentSetting(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Boolean enabled) {
        log.info("▶▶▶ [API Start] updateAutoBudgetAdjustmentSetting | enabled: {}", enabled);
        userInfoService.updateAutoBudgetAdjustmentSetting(userDetails.getUsername(), enabled);
        return ResponseEntity.ok().build();
    }
}
