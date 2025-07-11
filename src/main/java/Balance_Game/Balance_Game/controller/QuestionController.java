// src/main/java/Balance_Game/Balance_Game/controller/QuestionController.java
package Balance_Game.Balance_Game.controller;

import Balance_Game.Balance_Game.dto.PopularQuestionDto;
import Balance_Game.Balance_Game.dto.QuestionCreateRequestDto;
import Balance_Game.Balance_Game.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    // 인기 질문 목록 페이징 조회 (GET /api/questions/popular?page=0&size=20)
    @GetMapping("/popular")
    public ResponseEntity<Page<PopularQuestionDto>> getPopularQuestions(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PopularQuestionDto> popularQuestions = questionService.getPopularQuestions(pageable);
        return ResponseEntity.ok(popularQuestions);
    }

    // 새로운 질문 생성 (POST /api/questions)
    @PostMapping // consumes 속성이 없고, MultipartFile 파라미터가 없습니다.
    public ResponseEntity<Long> createQuestion(
            @RequestBody QuestionCreateRequestDto requestDto, // 오직 JSON 데이터만 받습니다.
            @AuthenticationPrincipal UserDetails userDetails) {

        // 서비스에 DTO와 사용자 정보만 간단하게 전달합니다.
        Long questionId = questionService.createQuestion(requestDto, userDetails.getUsername());
        return ResponseEntity.ok(questionId);
    }
}
