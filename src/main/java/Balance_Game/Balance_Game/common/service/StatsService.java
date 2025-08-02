package Balance_Game.Balance_Game.common.service;

import Balance_Game.Balance_Game.common.dto.StatsResponseDto;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.question.repository.QuestionBundleRepository;
import Balance_Game.Balance_Game.question.repository.QuestionBundleStatsRepository;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 전체 통계 정보를 관리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final QuestionRepository questionRepository;
    private final QuestionBundleRepository questionBundleRepository;
    private final QuestionBundleStatsRepository questionBundleStatsRepository;
    private final UserRepository userRepository;

    /**
     * 홈페이지용 전체 통계 정보를 조회합니다.
     * 성능 최적화를 위해 캐싱을 적용합니다.
     *
     * @return StatsResponseDto 전체 통계 정보
     */
    @Cacheable(value = "overallStats", cacheManager = "cacheManager")
    public StatsResponseDto getOverallStats() {
        log.info("전체 통계 정보 조회 시작");

        try {
            // 1. 전체 질문 수 조회
            Long totalQuestions = questionRepository.countByIsActiveTrue();

            // 2. 전체 질문 묶음 수 조회
            Long totalBundles = questionBundleRepository.count();

            // 3. 전체 게임 플레이 횟수 조회 (모든 질문 묶음의 플레이 카운트 합계)
            Long totalPlays = questionBundleStatsRepository.sumAllPlayCounts();

            // 4. 활성 사용자 수 조회 (최근 30일 내 활동한 사용자)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long activeUsers = userRepository.countActiveUsersAfter(thirtyDaysAgo);

            StatsResponseDto stats = StatsResponseDto.builder()
                    .totalQuestions(totalQuestions != null ? totalQuestions : 0L)
                    .totalBundles(totalBundles != null ? totalBundles : 0L)
                    .totalPlays(totalPlays != null ? totalPlays : 0L)
                    .activeUsers(activeUsers != null ? activeUsers : 0L)
                    .build();

            log.info("전체 통계 정보 조회 완료 - Questions: {}, Bundles: {}, Plays: {}, ActiveUsers: {}",
                    stats.getTotalQuestions(), stats.getTotalBundles(),
                    stats.getTotalPlays(), stats.getActiveUsers());

            return stats;

        } catch (Exception e) {
            log.error("통계 정보 조회 중 오류 발생", e);
            // 오류 발생 시 기본값 반환
            return StatsResponseDto.builder()
                    .totalQuestions(0L)
                    .totalBundles(0L)
                    .totalPlays(0L)
                    .activeUsers(0L)
                    .build();
        }
    }
}