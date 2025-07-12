package Balance_Game.Balance_Game.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // [수정된 부분]
        // CustomOAuth2UserService에서 이미 모든 처리를 마친 'Authentication' 객체가 파라미터로 전달됩니다.
        // 따라서 이 객체를 직접 사용하여 토큰을 생성하면 됩니다.
        String accessToken = jwtTokenProvider.createToken(authentication);

        // 프론트엔드로 리디렉션할 URL을 생성합니다. 토큰을 쿼리 파라미터로 추가합니다.
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        log.info("소셜 로그인 성공. 리디렉션 URL: {}", targetUrl);

        // 생성된 URL로 리디렉션시킵니다.
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
