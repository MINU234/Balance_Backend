package Balance_Game.Balance_Game.common.scheduler;

import Balance_Game.Balance_Game.game.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShareCodeExpirationScheduler {
    
    private final GameSessionRepository gameSessionRepository;
    
    /**
     * 매일 자정에 만료된 공유 코드를 정리합니다.
     * cron = "0 0 0 * * *" : 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOldShareCodes() {
        log.info("공유 코드 만료 처리 시작");
        
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = gameSessionRepository.expireShareCodesOlderThan(now);
        
        log.info("만료된 공유 코드 {} 개를 정리했습니다.", expiredCount);
    }
    
    /**
     * 매주 일요일 자정에 오래된 임시 사용자 세션을 정리합니다.
     * 단, UserAnswer는 통계 데이터 보존을 위해 삭제하지 않습니다.
     */
    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void cleanupOldTempUserSessions() {
        log.info("오래된 임시 사용자 세션 정리 시작");
        
        // 30일 이상 된 임시 사용자 세션만 삭제 (UserAnswer는 FK 제약으로 인해 유지됨)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = gameSessionRepository.softDeleteOldTempUserSessions(cutoffDate);
        
        log.info("{}개의 오래된 임시 사용자 세션을 처리했습니다.", deletedCount);
    }
}
