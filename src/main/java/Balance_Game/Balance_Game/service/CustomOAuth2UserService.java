package Balance_Game.Balance_Game.service;

import Balance_Game.Balance_Game.entity.User;
import Balance_Game.Balance_Game.repository.UserRepository;
import Balance_Game.Balance_Game.security.dto.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService 객체 생성
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        // OAuth2UserService를 사용하여 OAuth2User 정보를 가져옴
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        // 클라이언트 등록 ID(google, kakao)와 사용자 이름 속성을 가져옴
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // OAuth2User 정보를 바탕으로 OAuthAttributes 객체를 생성
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 데이터베이스에서 사용자를 저장하거나 업데이트
        User user = saveOrUpdate(attributes);

        // 사용자의 권한 정보와 함께 DefaultOAuth2User 객체를 반환 (이것이 Spring Security의 Principal이 됨)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        // 이메일로 사용자를 찾음
        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());

        User user;
        if (userOptional.isPresent()) {
            // 이미 가입된 사용자인 경우, 소셜 로그인 정보로 업데이트
            user = userOptional.get();
            user.updateOAuthInfo(attributes.getNickname(), attributes.getProvider(), attributes.getProviderId());
        } else {
            // 처음 온 사용자인 경우, 새로운 User 엔티티를 생성하여 저장
            user = attributes.toEntity();
        }

        return userRepository.save(user);
    }
}
