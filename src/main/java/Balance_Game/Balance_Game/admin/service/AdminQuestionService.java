package Balance_Game.Balance_Game.admin.service;

import Balance_Game.Balance_Game.admin.dto.AdminDashboardStatsDto;
import Balance_Game.Balance_Game.question.dto.QuestionDto;
import Balance_Game.Balance_Game.question.entity.ApprovalStatus;
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.user.entity.User;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQuestionService {
    
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    
    /**
     * 승인 대기중인 질문 목록 조회
     */
    public Page<QuestionDto> getPendingQuestions(Pageable pageable) {
        return questionRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable)
                .map(QuestionDto::from);
    }
    
    /**
     * 질문 승인
     */
    @Transactional
    public void approveQuestion(Long questionId, String adminEmail) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
                
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
        
        question.approve(admin);
        log.info("질문 승인 완료: questionId={}, adminEmail={}", questionId, adminEmail);
    }
    
    /**
     * 질문 거절
     */
    @Transactional
    public void rejectQuestion(Long questionId, String reason, String adminEmail) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
                
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
        
        question.reject(admin, reason);
        log.info("질문 거절 완료: questionId={}, adminEmail={}, reason={}", questionId, adminEmail, reason);
    }
    
    /**
     * 질문 일괄 승인
     */
    @Transactional
    public void bulkApproveQuestions(List<Long> questionIds, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
        
        List<Question> questions = questionRepository.findAllById(questionIds);
        questions.forEach(question -> question.approve(admin));
        
        log.info("질문 일괄 승인 완료: count={}, adminEmail={}", questions.size(), adminEmail);
    }
    
    /**
     * 승인/거절 이력 조회
     */
    public Page<QuestionDto> getApprovalHistory(String status, Pageable pageable) {
        if (status != null) {
            ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
            return questionRepository.findByApprovalStatus(approvalStatus, pageable)
                    .map(QuestionDto::from);
        }
        return questionRepository.findByApprovalStatusNot(ApprovalStatus.PENDING, pageable)
                .map(QuestionDto::from);
    }
    /**
     * 관리자 대시보드 통계 조회
     */
    public AdminDashboardStatsDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        // 기본 통계
        long pendingCount = questionRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approvedCount = questionRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long rejectedCount = questionRepository.countByApprovalStatus(ApprovalStatus.REJECTED);
        long todayCount = questionRepository.countByCreatedAtAfter(startOfDay);
        long weekCount = questionRepository.countByCreatedAtAfter(startOfWeek);
        long monthCount = questionRepository.countByCreatedAtAfter(startOfMonth);
        
        // 최근 7일간 일별 통계
        List<AdminDashboardStatsDto.DailyStats> dailyStats = getDailyStats(7);
        
        // 키워드별 대기 질문 수
        List<AdminDashboardStatsDto.KeywordPendingStats> keywordStats = getKeywordPendingStats();
        
        // 관리자별 활동 통계
        List<AdminDashboardStatsDto.AdminActivityStats> adminActivityStats = getAdminActivityStats();
        
        return AdminDashboardStatsDto.builder()
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .dailyStats(dailyStats)
                .keywordStats(keywordStats)
                .adminActivityStats(adminActivityStats)
                .build();
    }
    
    private List<AdminDashboardStatsDto.DailyStats> getDailyStats(int days) {
        return questionRepository.getDailyStatsByApprovalStatus(LocalDateTime.now().minusDays(days))
                .stream()
                .map(stat -> AdminDashboardStatsDto.DailyStats.builder()
                        .date((LocalDate) stat[0])
                        .pendingCount((Long) stat[1])
                        .approvedCount((Long) stat[2])
                        .rejectedCount((Long) stat[3])
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<AdminDashboardStatsDto.KeywordPendingStats> getKeywordPendingStats() {
        return questionRepository.getKeywordStatsByApprovalStatus(ApprovalStatus.PENDING)
                .stream()
                .map(stat -> AdminDashboardStatsDto.KeywordPendingStats.builder()
                        .keyword((String) stat[0])
                        .count((Long) stat[1])
                        .build())
                .limit(10) // 상위 10개 키워드만
                .collect(Collectors.toList());
    }
    
    private List<AdminDashboardStatsDto.AdminActivityStats> getAdminActivityStats() {
        return userRepository.getAdminActivityStats()
                .stream()
                .map(stat -> AdminDashboardStatsDto.AdminActivityStats.builder()
                        .adminName((String) stat[0])
                        .approvedCount((Long) stat[1])
                        .rejectedCount((Long) stat[2])
                        .lastActivityDate(stat[3] != null ? ((LocalDateTime) stat[3]).toLocalDate() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
