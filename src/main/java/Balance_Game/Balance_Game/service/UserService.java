// src/main/java/Balance_Game/Balance_Game/service/UserService.java
package Balance_Game.Balance_Game.service;

import Balance_Game.Balance_Game.dto.LoginRequestDto;
import Balance_Game.Balance_Game.dto.TokenDto;
import Balance_Game.Balance_Game.dto.UserSignupRequestDto;
import Balance_Game.Balance_Game.entity.User;
import Balance_Game.Balance_Game.repository.UserRepository;
import Balance_Game.Balance_Game.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // 주입
    private final AuthenticationManagerBuilder authenticationManagerBuilder; // 주입

    @Transactional
    public Long signup(UserSignupRequestDto requestDto) {
        // 1. 이메일, 닉네임 중복 확인
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. 사용자 정보 생성 및 저장
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                // 일반 가입이므로 provider 정보는 null
                .build();

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }
    @Transactional
    public TokenDto login(LoginRequestDto loginRequestDto) {
        // 1. Login ID/PW를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        //    authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String jwt = jwtTokenProvider.createToken(authentication);

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(jwt)
                .build();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));
    }
}
