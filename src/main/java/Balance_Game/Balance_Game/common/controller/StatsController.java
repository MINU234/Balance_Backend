package Balance_Game.Balance_Game.common.controller;


import Balance_Game.Balance_Game.common.dto.StatsResponseDto;
import Balance_Game.Balance_Game.common.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 전체 통계 정보를 제공하는 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 홈페이지용 전체 통계 정보를 조회합니다.
     *
     * @return ResponseEntity<StatsResponseDto> 전체 통계 정보
     */
    @GetMapping
    public ResponseEntity<StatsResponseDto> getOverallStats() {
        log.info("전체 통계 정보 조회 API 호출");

        try {
            StatsResponseDto stats = statsService.getOverallStats();
            log.info("전체 통계 정보 조회 API 응답 성공");
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("전체 통계 정보 조회 API 오류", e);
            // 오류 발생 시에도 기본값으로 응답하여 프론트엔드가 정상 동작하도록 함
            StatsResponseDto fallbackStats = StatsResponseDto.builder()
                    .totalQuestions(0L)
                    .totalBundles(0L)
                    .totalPlays(0L)
                    .activeUsers(0L)
                    .build();
            return ResponseEntity.ok(fallbackStats);
        }
    }
}