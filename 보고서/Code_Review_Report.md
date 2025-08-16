# 🔍 Balance Game 코드 리뷰 및 개선사항 보고서

## 📋 개요

Balance Game 프로젝트의 전체 코드베이스를 분석한 결과, 전반적으로 잘 구성된 Spring Boot 애플리케이션입니다. 하지만 몇 가지 중요한 개선점과 잠재적 이슈들이 발견되었습니다.

---

## 🚨 **발견된 이슈 및 수정 필요사항**

### 1. **심각한 보안 이슈**

#### 🔴 **CORS 설정 과도하게 관대함**
```java
// SecurityConfig.java:49
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
configuration.setAllowedHeaders(Arrays.asList("*")); // 🚨 모든 헤더 허용
```

**문제점:**
- `setAllowedHeaders("*")`는 보안 위험
- 프로덕션에서 모든 HTTP 메서드 허용은 위험

**해결책:**
```java
configuration.setAllowedHeaders(Arrays.asList(
    "Content-Type", "Authorization", "X-Requested-With"
));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
```

#### 🔴 **JWT 토큰 만료시간 관리 부재**
```java
// AuthService.java에서 토큰 생성 시 만료시간 설정이 명확하지 않음
String accessToken = jwtTokenProvider.createToken(authentication);
```

**문제점:**
- Refresh Token 로직 부재
- 토큰 만료 시 재로그인 강제

### 2. **데이터 정합성 문제**

#### 🟡 **공유코드 생성 시 무한루프 위험**
```java
// GamePlayService.java:178-185
int attempts = 0;
do {
    shareCode = shareCodeGenerator.generateShareCode();
    attempts++;
    if (attempts > 100) { // 🚨 하드코딩된 제한
        throw new RuntimeException("공유 코드 생성 실패");
    }
} while (gameSessionRepository.existsByShareCode(shareCode));
```

**문제점:**
- 사용자 증가 시 충돌 확률 증가
- 100번 시도 후 실패 - 사용자 경험 악화

**해결책:**
```java
// 1. 코드 길이 증가 (8자리 → 10자리)
// 2. 타임스탬프 기반 코드 생성
// 3. UUID 기반 접근법 고려
```

#### 🟡 **트랜잭션 범위 문제**
```java
// AuthService.java:13
@Transactional // 🚨 클래스 레벨 트랜잭션
public class AuthService {
    public String login(LoginRequestDto loginRequestDto) {
        // 로그인 로직 - 읽기 전용이어야 함
    }
}
```

**문제점:**
- 로그인은 읽기 전용 작업인데 쓰기 트랜잭션 사용
- 불필요한 락 발생 가능

### 3. **성능 이슈**

#### 🟡 **N+1 쿼리 문제**
```java
// GamePlayService.java:243-244
List<UserAnswer> originalAnswers = userAnswerRepository.findByGameSessionId(originalSession.getId());
List<UserAnswer> compareAnswers = userAnswerRepository.findByGameSessionId(compareSessionId);
```

**문제점:**
- 답변 조회 시 Question 엔티티 지연 로딩으로 N+1 발생
- 결과 비교 시 성능 저하

**해결책:**
```java
// Repository에서 fetch join 사용
@Query("SELECT ua FROM UserAnswer ua JOIN FETCH ua.question WHERE ua.gameSession.id = :sessionId")
List<UserAnswer> findByGameSessionIdWithQuestion(@Param("sessionId") Long sessionId);
```

#### 🟡 **캐싱 전략 부족**
```java
// QuestionService.java:32
@Cacheable(value = "popularQuestions", key = "#pageable.pageNumber", cacheManager = "cacheManager")
```

**문제점:**
- 페이지별 캐싱만 있고, 자주 조회되는 데이터 캐싱 부족
- 캐시 무효화 정책 부재

### 4. **비즈니스 로직 이슈**

#### 🟡 **공유코드 만료 로직 불완전**
```java
// GameSession.java:104-109
public boolean isShareCodeValid() {
    if (this.shareCode == null || this.shareCodeExpiresAt == null) {
        return false;
    }
    return LocalDateTime.now().isBefore(this.shareCodeExpiresAt);
}
```

**문제점:**
- 만료된 코드 자동 정리 로직 부재
- 스케줄러 존재하지만 실제 활용도 불명확

#### 🟡 **임시 사용자 관리 문제**
```java
// GamePlayService.java:66-71
String tempUserId = null;
if (player == null && requestDto.getTempUserId() != null) {
    tempUserId = requestDto.getTempUserId();
} else if (player == null) {
    tempUserId = UUID.randomUUID().toString(); // 🚨 프론트엔드와 불일치 가능
}
```

**문제점:**
- 프론트엔드에서 생성한 tempUserId와 백엔드 생성 ID 간 혼동
- 비회원 세션 관리 일관성 부족

---

## 🔧 **개선 사항**

### 1. **보안 강화**

#### JWT 보안 개선
```java
@Component
public class JwtTokenProvider {
    
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일
    
    public TokenDto createTokenDto(Authentication authentication) {
        return TokenDto.builder()
            .accessToken(createToken(authentication, ACCESS_TOKEN_EXPIRE_TIME))
            .refreshToken(createRefreshToken(authentication, REFRESH_TOKEN_EXPIRE_TIME))
            .build();
    }
}
```

#### 입력 검증 강화
```java
// DTO에 적절한 검증 어노테이션 추가
public class QuestionCreateRequestDto {
    @NotBlank(message = "질문 내용은 필수입니다")
    @Size(max = 200, message = "질문은 200자 이내로 작성해주세요")
    private String text;
    
    @Pattern(regexp = "^[A-Za-z0-9가-힣\\s]+$", message = "특수문자는 사용할 수 없습니다")
    private String keyword;
}
```

### 2. **성능 최적화**

#### 데이터베이스 인덱스 추가
```sql
-- 자주 조회되는 패턴에 대한 복합 인덱스
CREATE INDEX idx_question_approval_keyword ON question(approval_status, keyword);
CREATE INDEX idx_game_session_bundle_status ON game_session(bundle_id, session_status);
CREATE INDEX idx_user_answer_session_question ON user_answer(game_session_id, question_id);
```

#### 배치 처리 최적화
```java
@Service
public class StatisticsService {
    
    @Scheduled(fixedRate = 3600000) // 1시간마다
    @Transactional
    public void updatePopularityScores() {
        // 인기도 점수 일괄 업데이트
        questionRepository.updatePopularityScores();
    }
}
```

### 3. **코드 품질 향상**

#### 상수 관리 개선
```java
public class GameConstants {
    public static final int SHARE_CODE_LENGTH = 8;
    public static final int SHARE_CODE_EXPIRE_DAYS = 3;
    public static final int MAX_SHARE_CODE_ATTEMPTS = 100;
    public static final String SHARE_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
}
```

#### 예외 처리 세분화
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ShareCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleShareCodeExpired(ShareCodeExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
            .body(new ErrorResponse("SHARE_CODE_EXPIRED", ex.getMessage()));
    }
    
    @ExceptionHandler(GameSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameSessionNotFound(GameSessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("GAME_SESSION_NOT_FOUND", ex.getMessage()));
    }
}
```

---

## 🚀 **추가하면 좋을 기능들**

### 1. **운영 효율성 기능**

#### 관리자 대시보드 강화
```java
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    
    @GetMapping("/real-time-stats")
    public ResponseEntity<RealTimeStatsDto> getRealTimeStats() {
        return ResponseEntity.ok(RealTimeStatsDto.builder()
            .activeGames(gameService.getActiveGameCount())
            .pendingQuestions(questionService.getPendingCount())
            .dailyActiveUsers(userService.getDailyActiveUsers())
            .systemHealth(systemService.getHealthStatus())
            .build());
    }
}
```

#### 자동화된 콘텐츠 검토
```java
@Service
public class ContentModerationService {
    
    public ModerationResult moderateQuestion(Question question) {
        // 1. 욕설/부적절한 콘텐츠 자동 감지
        // 2. 중복 질문 검사
        // 3. 품질 점수 계산
        return ModerationResult.builder()
            .autoApproved(isAutoApprovable(question))
            .flaggedReasons(getFlaggedReasons(question))
            .qualityScore(calculateQualityScore(question))
            .build();
    }
}
```

### 2. **사용자 경험 향상**

#### 추천 시스템
```java
@Service
public class RecommendationService {
    
    public List<QuestionBundleDto> getPersonalizedRecommendations(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        
        // 1. 사용자 플레이 히스토리 분석
        // 2. 선호 키워드 추출
        // 3. 유사 사용자 기반 추천
        return recommendationEngine.generateRecommendations(user);
    }
}
```

#### 소셜 기능
```java
@Entity
public class UserFollow {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User follower;
    
    @ManyToOne
    private User following;
    
    @CreatedDate
    private LocalDateTime createdAt;
}

@RestController
@RequestMapping("/api/social")
public class SocialController {
    
    @PostMapping("/follow")
    public ResponseEntity<Void> followUser(@RequestParam String targetEmail) {
        socialService.followUser(getCurrentUser(), targetEmail);
        return ResponseEntity.ok().build();
    }
}
```

### 3. **분석 및 인사이트**

#### 상세 게임 분석
```java
@Service
public class GameAnalyticsService {
    
    public GameInsightDto analyzeGameSession(Long sessionId) {
        return GameInsightDto.builder()
            .avgResponseTime(calculateAvgResponseTime(sessionId))
            .difficultyDistribution(getDifficultyDistribution(sessionId))
            .popularChoices(getPopularChoices(sessionId))
            .comparisonStats(getComparisonStats(sessionId))
            .build();
    }
}
```

#### A/B 테스트 프레임워크
```java
@Service
public class ABTestService {
    
    public boolean isEnabledForUser(String testName, String userIdentifier) {
        ABTest test = abTestRepository.findByName(testName);
        return test != null && test.isUserInTestGroup(userIdentifier);
    }
}
```

### 4. **시스템 안정성**

#### Rate Limiting
```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.tryAcquire(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        }
        
        return true;
    }
}
```

#### 서킷 브레이커 패턴
```java
@Service
public class ExternalServiceClient {
    
    @CircuitBreaker(name = "azure-storage", fallbackMethod = "fallbackUpload")
    public String uploadImage(MultipartFile file) {
        return azureUploadService.upload(file);
    }
    
    public String fallbackUpload(MultipartFile file, Exception ex) {
        log.error("Image upload failed, using fallback", ex);
        return "/images/default-placeholder.png";
    }
}
```

### 5. **모니터링 및 관찰성**

#### 메트릭 수집
```java
@Component
public class GameMetrics {
    
    private final Counter gameStartCounter = Counter.builder("game.start")
        .description("Number of games started")
        .register(Metrics.globalRegistry);
    
    private final Timer gameCompletionTimer = Timer.builder("game.completion")
        .description("Game completion time")
        .register(Metrics.globalRegistry);
}
```

#### 구조화된 로깅
```java
@Slf4j
@Service
public class GamePlayService {
    
    public GameSessionResponseDto startGame(StartGameRequestDto requestDto) {
        log.info("Game started: bundleId={}, userType={}, ip={}", 
            requestDto.getBundleId(),
            requestDto.getUserEmail() != null ? "member" : "guest",
            getCurrentUserIp());
        
        // 게임 로직...
    }
}
```

---

## 📊 **우선순위별 개선 로드맵**

### 🔴 **Phase 1: 긴급 (1-2주)**
1. **보안 강화**
   - CORS 설정 최소화
   - JWT Refresh Token 구현
   - 입력 검증 강화

2. **성능 최적화**
   - N+1 쿼리 해결
   - 데이터베이스 인덱스 추가
   - 공유코드 생성 알고리즘 개선

### 🟡 **Phase 2: 중요 (3-4주)**
1. **모니터링 시스템**
   - 메트릭 수집
   - 로깅 체계 구축
   - 알림 시스템

2. **관리자 도구 강화**
   - 실시간 대시보드
   - 자동 콘텐츠 검토
   - 사용자 관리 기능

### 🟢 **Phase 3: 개선 (5-8주)**
1. **사용자 경험**
   - 추천 시스템
   - 소셜 기능
   - 게임 분석 도구

2. **시스템 확장성**
   - Rate Limiting
   - 캐싱 전략
   - A/B 테스트 프레임워크

---

## 💡 **즉시 적용 가능한 개선사항**

### 1. **환경변수 관리**
```yaml
# application.yml
app:
  game:
    share-code:
      length: ${SHARE_CODE_LENGTH:8}
      expire-days: ${SHARE_CODE_EXPIRE_DAYS:3}
      max-attempts: ${MAX_SHARE_CODE_ATTEMPTS:50}
  security:
    jwt:
      access-token-expire: ${JWT_ACCESS_EXPIRE:1800000} # 30분
      refresh-token-expire: ${JWT_REFRESH_EXPIRE:604800000} # 7일
```

### 2. **로깅 개선**
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <mdc/>
                <message/>
            </providers>
        </encoder>
    </appender>
</configuration>
```

### 3. **헬스체크 강화**
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        boolean isHealthy = checkDatabaseConnection() && 
                           checkAzureStorage() && 
                           checkMemoryUsage();
        
        if (isHealthy) {
            return Health.up()
                .withDetail("database", "Connected")
                .withDetail("storage", "Available")
                .build();
        } else {
            return Health.down()
                .withDetail("error", "Service degraded")
                .build();
        }
    }
}
```

---

## 🎯 **결론**

Balance Game 프로젝트는 전반적으로 잘 설계된 애플리케이션이지만, **보안과 성능 측면에서 몇 가지 중요한 개선이 필요**합니다. 

**강점:**
- ✅ 명확한 계층 구조
- ✅ 적절한 엔티티 설계
- ✅ Spring Security 활용

**개선 필요:**
- 🔴 보안 강화 (CORS, JWT)
- 🔴 성능 최적화 (N+1, 인덱싱)
- 🟡 모니터링 체계 구축

권장사항은 우선순위에 따라 단계적으로 적용하여 서비스의 안정성과 확장성을 확보하는 것입니다.