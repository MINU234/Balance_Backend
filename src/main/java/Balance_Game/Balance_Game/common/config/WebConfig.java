package Balance_Game.Balance_Game.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS 설정은 SecurityConfig에서 통합 관리하므로 여기서는 제거
}
