// src/main/java/Balance_Game/Balance_Game/common/controller/MyPageController.java
package Balance_Game.Balance_Game.common.controller;

import Balance_Game.Balance_Game.question.dto.PopularBundleDto;
import Balance_Game.Balance_Game.question.dto.QuestionDto;
import Balance_Game.Balance_Game.question.entity.ApprovalStatus;
import Balance_Game.Balance_Game.question.service.QuestionBundleService;
import Balance_Game.Balance_Game.question.service.QuestionService;
import Balance_Game.Balance_Game.game.dto.GameResultDto;
import Balance_Game.Balance_Game.game.service.GamePlayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyPageController {

    private final QuestionBundleService questionBundleService;
    private final QuestionService questionService;
    private final GamePlayService gamePlayService;
    private final MyPageService myPageService;

    /**
     * 내가 만든 질문 묶음 조회
     */
    @GetMapping("/question-bundles")
    public ResponseEntity<Page<PopularBundleDto>> getMyQuestionBundles(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PopularBundleDto> myBundles =
                questionBundleService.findByCreatorEmail(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(myBundles);
    }

    /**
     * 내가 만든 질문 조회 (모든 상태)
     */
    @GetMapping("/questions")
    public ResponseEntity<Page<QuestionDto>> getMyQuestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<QuestionDto> myQuestions =
                questionService.findByCreatorEmail(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(myQuestions);
    }
    
    /**
     * 내 질문 승인 상태별 조회
     */
    @GetMapping("/questions/status/{status}")
    public ResponseEntity<Page<QuestionDto>> getMyQuestionsByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String status,
            @PageableDefault(size = 10) Pageable pageable) {
        ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
        Page<QuestionDto> questions = 
                myPageService.getMyQuestionsByStatus(userDetails.getUsername(), approvalStatus, pageable);
        return ResponseEntity.ok(questions);
    }
    
    /**
     * 내 게임 기록 조회
     */
    @GetMapping("/game-history")
    public ResponseEntity<Page<GameResultDto>> getMyGameHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<GameResultDto> gameHistory = 
                myPageService.getMyGameHistory(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(gameHistory);
    }
    
    /**
     * 내 통계 조회 (생성한 질문 수, 묶음 수, 플레이 횟수 등)
     */
    @GetMapping("/stats")
    public ResponseEntity<MyPageStatsDto> getMyStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        MyPageStatsDto stats = myPageService.getMyStats(userDetails.getUsername());
        return ResponseEntity.ok(stats);
    }
}
