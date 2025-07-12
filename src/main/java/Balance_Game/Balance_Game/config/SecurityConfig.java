// src/main/java/Balance_Game/Balance_Game/config/SecurityConfig.java
package Balance_Game.Balance_Game.config;

import Balance_Game.Balance_Game.security.JwtAuthenticationFilter;
import Balance_Game.Balance_Game.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableWebSecurity // Spring Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입


    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈 등록
     * BCrypt 알고리즘을 사용합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 위한 빈 등록
     * 프론트엔드(예: React, Vue) 서버와의 통신을 허용하기 위해 필요합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 개발 서버 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }

    /**
     * HTTP 보안 설정을 위한 SecurityFilterChain 빈 등록
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. REST API이므로 CSRF, Form-Login, HTTP-Basic 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. 세션을 사용하지 않는 Stateless 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. API 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // --- 누구나 접근 가능한 URL ---
                        .requestMatchers(
                                "/api/auth/**",       // 회원가입, 로그인 등 인증 관련
                                "/api/questions/popular", // 인기 질문 목록 조회
                                "/api/collections/public", // 공개 질문 묶음 목록 조회
                                "/api/collections/{collectionId}" // 특정 질문 묶음 상세 조회 (GET 요청)
                        ).permitAll()
                        // --- 관리자(ADMIN)만 접근 가능한 URL ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // --- 일반 사용자(USER)만 접근 가능한 URL ---
                        .requestMatchers(
                                "/api/images/upload",
                                "/api/question-bundles/**", // 질문 묶음 생성 등
                                "/api/mypage/**"
                        ).hasRole("USER")

                        // --- 나머지 모든 요청은 인증 필요 ---
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
