package Balance_Game.Balance_Game.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 게임 시작 카운터
    private final Counter gameStartCounter = Counter.builder("balance.game.start")
            .description("Number of games started")
            .register(meterRegistry);
    
    // 게임 완료 카운터
    private final Counter gameCompleteCounter = Counter.builder("balance.game.complete")
            .description("Number of games completed")
            .register(meterRegistry);
    
    // 공유코드 생성 카운터
    private final Counter shareCodeGeneratedCounter = Counter.builder("balance.sharecode.generated")
            .description("Number of share codes generated")
            .register(meterRegistry);
    
    // 결과 비교 카운터
    private final Counter resultCompareCounter = Counter.builder("balance.result.compare")
            .description("Number of result comparisons")
            .register(meterRegistry);
    
    // 질문 생성 요청 카운터
    private final Counter questionCreateCounter = Counter.builder("balance.question.create")
            .description("Number of question creation requests")
            .register(meterRegistry);
    
    // 질문 승인 카운터
    private final Counter questionApprovalCounter = Counter.builder("balance.question.approval")
            .description("Number of question approvals")
            .tag("result", "approved")
            .register(meterRegistry);
    
    // 질문 거절 카운터
    private final Counter questionRejectionCounter = Counter.builder("balance.question.approval")
            .description("Number of question rejections")
            .tag("result", "rejected")
            .register(meterRegistry);
    
    // 게임 플레이 시간 타이머
    private final Timer gamePlayTimer = Timer.builder("balance.game.duration")
            .description("Game completion time")
            .register(meterRegistry);
    
    // API 응답 시간 타이머
    private final Timer apiResponseTimer = Timer.builder("balance.api.response")
            .description("API response time")
            .register(meterRegistry);
    
    // 메트릭 증가 메서드들
    public void incrementGameStart() {
        gameStartCounter.increment();
    }
    
    public void incrementGameComplete() {
        gameCompleteCounter.increment();
    }
    
    public void incrementShareCodeGenerated() {
        shareCodeGeneratedCounter.increment();
    }
    
    public void incrementResultCompare() {
        resultCompareCounter.increment();
    }
    
    public void incrementQuestionCreate() {
        questionCreateCounter.increment();
    }
    
    public void incrementQuestionApproval() {
        questionApprovalCounter.increment();
    }
    
    public void incrementQuestionRejection() {
        questionRejectionCounter.increment();
    }
    
    public Timer.Sample startGameTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordGameDuration(Timer.Sample sample) {
        sample.stop(gamePlayTimer);
    }
    
    public Timer.Sample startApiTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordApiResponse(Timer.Sample sample, String endpoint) {
        sample.stop(Timer.builder("balance.api.response")
                .tag("endpoint", endpoint)
                .register(meterRegistry));
    }
}