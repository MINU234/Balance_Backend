package Balance_Game.Balance_Game.controller;

import Balance_Game.Balance_Game.dto.LoginRequestDto;
import Balance_Game.Balance_Game.dto.TokenDto;
import Balance_Game.Balance_Game.dto.UserSignupRequestDto;
import Balance_Game.Balance_Game.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenDto tokenDto = userService.login(loginRequestDto);
        return ResponseEntity.ok(tokenDto);
    }
}
