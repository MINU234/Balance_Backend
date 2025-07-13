package Balance_Game.Balance_Game.auth.controller;

import Balance_Game.Balance_Game.auth.dto.LoginRequestDto;
import Balance_Game.Balance_Game.user.dto.UserInfoDto;
import Balance_Game.Balance_Game.user.dto.UserSignupRequestDto;
import Balance_Game.Balance_Game.user.entity.User;
import Balance_Game.Balance_Game.auth.security.JwtAuthenticationFilter;
import Balance_Game.Balance_Game.auth.service.AuthService;
import Balance_Game.Balance_Game.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String accessToken = authService.login(loginRequestDto);

        // 1. ResponseCookie 객체 생성
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.AUTHORIZATION_COOKIE_NAME, accessToken)
                .httpOnly(true)    // JavaScript 접근 방지
                .secure(true)      // HTTPS 에서만 전송
                .path("/")         // 모든 경로에서 쿠키 유효
                .maxAge(60 * 60)   // 쿠키 만료 시간 (예: 1시간)
                .sameSite("Lax") // CSRF 방지
                .build();

        // 2. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 3. 응답 본문에는 성공 메시지만 포함
        return ResponseEntity.ok("로그인에 성공했습니다.");
    }

    /**
     * [신규] 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 쿠키 삭제를 위해 Max-Age=0으로 설정
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.AUTHORIZATION_COOKIE_NAME, "")
                .maxAge(0)
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    /**
     * [신규] 인증 상태 확인 API
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        // @AuthenticationPrincipal을 통해 현재 인증된 사용자의 정보를 가져옴
        User user = userService.findByEmail(userDetails.getUsername());
        UserInfoDto userInfo = UserInfoDto.from(user);

        return ResponseEntity.ok(userInfo);
    }
}
