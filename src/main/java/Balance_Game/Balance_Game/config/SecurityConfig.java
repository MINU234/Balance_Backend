package Balance_Game.Balance_Game.config;

import Balance_Game.Balance_Game.repository.UserRepository;
import Balance_Game.Balance_Game.security.JwtAuthenticationFilter;
import Balance_Game.Balance_Game.security.JwtTokenProvider;
import Balance_Game.Balance_Game.security.OAuth2LoginSuccessHandler;
import Balance_Game.Balance_Game.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService; // [추가 ①] 커스텀 서비스 주입
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // [추가 ②] 성공 핸들러 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 기존 설정 (CORS, CSRF, 세션 관리 등)은 그대로 유지
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. API 엔드포인트별 접근 권한 재설정
                .authorizeHttpRequests(authz -> authz
                        // --- 누구나 접근 가능한 URL 목록 ---
                        .requestMatchers(
                                "/api/auth/**",      // 일반 로그인/회원가입
                                "/api/game/**",      // 게임 플레이 (비회원 가능)
                                "/oauth2/**"         // [추가] 소셜 로그인 관련 경로는 반드시 허용
                        ).permitAll()
                        // --- 특정 GET 요청은 누구나 가능하도록 구체화 ---
                        .requestMatchers(HttpMethod.GET,
                                "/api/questions/popular",
                                "/api/question-bundles/popular",
                                "/api/question-bundles/{id}"
                        ).permitAll()
                        // --- 나머지 모든 요청은 인증된 사용자만 접근 가능 ---
                        .anyRequest().authenticated()
                )

                // 3. [핵심 추가] OAuth2 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // (1) 소셜 로그인 성공 후, 우리가 만든 핸들러가 실행되도록 설정
                        .successHandler(oAuth2LoginSuccessHandler)
                        // (2) 소셜 로그인 성공 시 받아온 사용자 정보를 처리할 서비스를 지정
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )

                // 4. 모든 요청 전에 우리가 만든 JWT 필터를 실행하도록 설정
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
