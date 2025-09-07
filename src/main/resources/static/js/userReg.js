$(document).ready(function () {
    let emailVerified = false;

    // ID 중복 확인
    $("#btnUserId").click(function () {
        const user_id = $("#user_id").val().trim();
        const $msg = $("#userIdMsg");

        if (!user_id) {
            $msg.text("아이디를 입력해주세요.").removeClass().addClass("text-sm text-red-500");
            return;
        }

        $.ajax({
            url: "/user/getUserIdExists",
            type: "get",
            data: { user_id },
            success: function (res) {
                if (res.exist_yn === "Y") {
                    $msg.text("이미 사용 중인 아이디입니다.").removeClass().addClass("text-sm text-red-500");
                } else {
                    $msg.text("사용 가능한 아이디입니다.").removeClass().addClass("text-sm text-green-600");
                }
            }
        });
    });

    // 이메일 인증코드 발송
    $("#btnSendVerification").click(function () {
        const email = $("#email").val().trim();
        const $msg = $("#emailMsg");

        if (!email) {
            $msg.text("이메일을 입력해주세요.").removeClass().addClass("text-sm text-red-500");
            return;
        }

        // ✅ 이메일 중복 여부 먼저 체크
        $.ajax({
            url: "/user/getEmailExists",
            type: "POST",
            data: { email },
            success: function (res) {
                if (res.exist_yn === "Y") {
                    $msg.text("이미 사용 중인 이메일입니다.").removeClass().addClass("text-sm text-red-500");
                    return;
                }

                // ✅ 이메일이 중복되지 않은 경우에만 메일 발송
                $.ajax({
                    url: "/user/sendVerificationEmail",
                    type: "POST",
                    data: { email },
                    success: function (res) {
                        if (res.msg === "이메일을 확인하세요.") {
                            $msg.text(res.msg).removeClass().addClass("text-sm text-red-600");
                        } else {
                            $msg.text(res.msg).removeClass().addClass("text-sm text-green-600");
                        }
                    },
                    error: function () {
                        $msg.text("서버 오류가 발생했습니다.").removeClass().addClass("text-sm text-red-500");
                    }
                });
            },
            error: function () {
                $msg.text("이메일 중복 체크 중 오류 발생").removeClass().addClass("text-sm text-red-500");
            }
        });
    });

    // 이메일 인증코드 확인
    $("#btnVerifyCode").click(function () {
        const email = $("#email").val().trim();
        const code = $("#emailCode").val().trim();
        const $msg = $("#emailCodeMsg");

        if (!email || !code) {
            $msg.text("이메일과 인증 코드를 모두 입력해주세요.").removeClass().addClass("text-sm text-red-500");
            return;
        }

        $.ajax({
            url: "/user/verifyEmailCode",
            type: "POST",
            data: { email, code },
            success: function (res) {
                if (res.result === 1) {
                    emailVerified = true;
                    $msg.text("인증 성공").removeClass().addClass("text-sm text-green-600");
                } else {
                    $msg.text("인증 실패").removeClass().addClass("text-sm text-red-500");
                }
            }
        });
    });

    // 비밀번호 확인
    $("#password2").on("input", function () {
        const pw1 = $("#password").val();
        const pw2 = $("#password2").val();
        const $msg = $("#pwdCheckMsg");

        if (pw1 !== pw2) {
            $msg.text("비밀번호가 일치하지 않습니다.").removeClass().addClass("text-sm text-red-500");
        } else {
            $msg.text("비밀번호 일치").removeClass().addClass("text-sm text-green-600");
        }
    });

    // 회원가입 버튼 클릭 시 검증
    $("#btnSend").click(function (e) {
        e.preventDefault();

        const user_id = $("#user_id").val().trim();
        const password = $("#password").val().trim();
        const password2 = $("#password2").val().trim();
        const email = $("#email").val().trim();
        const name = $("#name").val().trim();
        const gender = $("#gender").val();
        const birthDate = $("#birthDate").val();

        let valid = true;

        if (!user_id) {
            $("#userIdMsg").text("아이디를 입력해주세요.").removeClass().addClass("text-sm text-red-500");
            valid = false;
        }

        if (!password || !password2 || password !== password2) {
            $("#pwdCheckMsg").text("비밀번호가 일치하지 않습니다.").removeClass().addClass("text-sm text-red-500");
            valid = false;
        }

        if (!email) {
            $("#emailMsg").text("이메일을 입력해주세요.").removeClass().addClass("text-sm text-red-500");
            valid = false;
        }

        if (!name) {
            $("#nameCheckMsg").text("이름을 입력해주세요.").removeClass().addClass("text-sm text-red-500");
            valid = false;
        }

        if (!emailVerified) {
            $("#emailCodeMsg").text("이메일 인증을 완료해야 합니다.").removeClass().addClass("text-sm text-red-500");
            valid = false;
        }

        if (!valid) return;

        // 회원가입 Ajax
        $.ajax({
            url: "/user/insertUserInfo",
            type: "POST",
            data: { user_id, password, email, name, gender, birthDate },
            success: function (res) {
                if (res.result === 1) {
                    alert(res.msg);
                    location.href = "/user/login";
                } else {
                    alert("회원가입 실패");
                }
            },
            error: function () {
                alert("회원가입 중 오류 발생");
            }
        });
    });
});