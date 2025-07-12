package Balance_Game.Balance_Game.service;

import Balance_Game.Balance_Game.dto.LoginRequestDto;
import Balance_Game.Balance_Game.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(LoginRequestDto loginRequestDto) {
        // 1. 사용자로부터 받은 이메일과 비밀번호로 '미인증' 토큰 객체를 생성합니다.
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        // 2. AuthenticationManager에 이 토큰을 전달하여 실제 인증을 수행합니다.
        //    이 과정에서 CustomUserDetailsService가 호출되어 DB의 사용자와 비밀번호를 비교합니다.
        //    인증에 실패하면 여기서 예외가 발생합니다.
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증이 성공하면, 인증된 정보를 바탕으로 JWT 토큰을 생성합니다.
        String accessToken = jwtTokenProvider.createToken(authentication);

        return accessToken;
    }
}

