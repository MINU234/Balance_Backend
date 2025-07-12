package Balance_Game.Balance_Game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 정책 적용
                .allowedOrigins("http://localhost:3000") // 1. 프론트엔드 주소 명시
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 요청 헤더 허용
                .allowCredentials(true) // 2. 쿠키를 포함한 요청 허용 (가장 중요!)
                .maxAge(3600); // Pre-flight 요청의 캐시 시간 (초)
    }
}
