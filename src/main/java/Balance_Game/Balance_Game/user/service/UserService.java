// src/main/java/Balance_Game/Balance_Game/service/UserService.java
package Balance_Game.Balance_Game.user.service;

import Balance_Game.Balance_Game.auth.dto.LoginRequestDto;
import Balance_Game.Balance_Game.auth.dto.TokenDto;
import Balance_Game.Balance_Game.user.dto.UserSignupRequestDto;
import Balance_Game.Balance_Game.user.dto.UserInfoDto;
import Balance_Game.Balance_Game.user.entity.User;
import Balance_Game.Balance_Game.user.entity.Role;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import Balance_Game.Balance_Game.auth.security.JwtTokenProvider;
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

        // 3. 사용자 정보 생성 및 저장 (Role 확실히 설정)
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .role(Role.USER)  // 기본 역할 USER로 설정 - 필수!
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

    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(String email) {
        User user = findByEmail(email);
        return UserInfoDto.from(user);
    }

    @Transactional(readOnly = true)
    public UserInfoDto getUserInfoById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + userId));
        return UserInfoDto.from(user);
    }

    @Transactional
    public UserInfoDto updateNickname(String email, String newNickname) {
        // 닉네임 중복 체크
        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = findByEmail(email);
        // User 엔티티에 updateNickname 메서드가 있다면 사용, 없다면 직접 필드 변경
        // 현재는 필드 변경 방식으로 구현
        User updatedUser = User.builder()
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(newNickname)
                .role(user.getRole())
                .build();

        User savedUser = userRepository.save(updatedUser);
        return UserInfoDto.from(savedUser);
    }
}
