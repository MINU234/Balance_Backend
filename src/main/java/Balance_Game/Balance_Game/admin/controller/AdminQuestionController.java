package Balance_Game.Balance_Game.admin.controller;

import Balance_Game.Balance_Game.admin.dto.AdminDashboardStatsDto;
import Balance_Game.Balance_Game.admin.dto.QuestionApprovalDto;
import Balance_Game.Balance_Game.admin.dto.QuestionRejectDto;
import Balance_Game.Balance_Game.admin.service.AdminQuestionService;
import Balance_Game.Balance_Game.question.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/questions")
@PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
public class AdminQuestionController {
    
    private final AdminQuestionService adminQuestionService;
    
    /**
     * 승인 대기중인 질문 목록 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<QuestionDto>> getPendingQuestions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminQuestionService.getPendingQuestions(pageable));
    }
    
    /**
     * 관리자 대시보드 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminQuestionService.getDashboardStats());
    }
    
    /**
     * 특정 질문 승인
     */
    @PostMapping("/{questionId}/approve")
    public ResponseEntity<Void> approveQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminQuestionService.approveQuestion(questionId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    /**
     * 특정 질문 거절
     */
    @PostMapping("/{questionId}/reject")
    public ResponseEntity<Void> rejectQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRejectDto rejectDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminQuestionService.rejectQuestion(questionId, rejectDto.getReason(), userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    /**
     * 질문 일괄 승인
     */
    @PostMapping("/bulk-approve")
    public ResponseEntity<Void> bulkApproveQuestions(
            @RequestBody QuestionApprovalDto approvalDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminQuestionService.bulkApproveQuestions(approvalDto.getQuestionIds(), userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    /**
     * 승인/거절된 질문 이력 조회
     */
    @GetMapping("/history")
    public ResponseEntity<Page<QuestionDto>> getApprovalHistory(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminQuestionService.getApprovalHistory(status, pageable));
    }
}
