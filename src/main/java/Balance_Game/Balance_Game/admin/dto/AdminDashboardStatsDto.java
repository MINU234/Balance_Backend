package Balance_Game.Balance_Game.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class AdminDashboardStatsDto {
    private long pendingCount;      // 승인 대기 수
    private long approvedCount;     // 승인된 수
    private long rejectedCount;     // 거절된 수
    private long todayCount;        // 오늘 등록된 수
    private long weekCount;         // 이번 주 등록된 수
    private long monthCount;        // 이번 달 등록된 수
    
    // 최근 7일간 일별 통계
    private List<DailyStats> dailyStats;
    
    // 키워드별 대기 질문 수
    private List<KeywordPendingStats> keywordStats;
    
    // 관리자별 처리 통계
    private List<AdminActivityStats> adminActivityStats;
    
    @Getter
    @Builder
    public static class DailyStats {
        private LocalDate date;
        private long pendingCount;
        private long approvedCount;
        private long rejectedCount;
    }
    
    @Getter
    @Builder
    public static class KeywordPendingStats {
        private String keyword;
        private long count;
    }
    
    @Getter
    @Builder
    public static class AdminActivityStats {
        private String adminName;
        private long approvedCount;
        private long rejectedCount;
        private LocalDate lastActivityDate;
    }
}
