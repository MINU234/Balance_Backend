// src/main/java/Balance_Game/Balance_Game/common/controller/MyPageController.java
package Balance_Game.Balance_Game.common.controller;

import Balance_Game.Balance_Game.question.dto.PopularBundleDto;
import Balance_Game.Balance_Game.question.dto.QuestionDto;
import Balance_Game.Balance_Game.question.service.QuestionBundleService;
import Balance_Game.Balance_Game.question.service.QuestionService;
import Balance_Game.Balance_Game.game.service.GamePlayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyPageController {

    private final QuestionBundleService questionBundleService;
    private final QuestionService questionService;
    private final GamePlayService gamePlayService;

    @GetMapping("/question-bundles")
    public ResponseEntity<Page<PopularBundleDto>> getMyQuestionBundles(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<PopularBundleDto> myBundles =
                questionBundleService.findByCreatorEmail(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(myBundles);
    }

    @GetMapping("/questions")
    public ResponseEntity<Page<QuestionDto>> getMyQuestions(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<QuestionDto> myQuestions =
                questionService.findByCreatorEmail(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(myQuestions);
    }
}
