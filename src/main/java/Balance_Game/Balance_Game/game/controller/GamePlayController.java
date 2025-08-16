package Balance_Game.Balance_Game.game.controller;

import Balance_Game.Balance_Game.game.dto.*;
import Balance_Game.Balance_Game.game.service.GamePlayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정 (프론트엔드 연동용)
public class GamePlayController {

    private final GamePlayService gamePlayService;

    /**
     * 게임 시작 (비회원/회원 모두 가능)
     */
    @PostMapping("/start")
    public ResponseEntity<GameSessionResponseDto> startGame(
            @Valid @RequestBody StartGameRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 로그인한 사용자라면 이메일 설정
        if (userDetails != null) {
            requestDto.setUserEmail(userDetails.getUsername());
        }
        
        GameSessionResponseDto response = gamePlayService.startGame(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 답변 제출
     */
    @PostMapping("/answer")
    public ResponseEntity<Void> submitAnswer(@Valid @RequestBody AnswerRequestDto answerRequestDto) {
        gamePlayService.recordAnswer(answerRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 게임 완료 및 공유 코드 받기
     */
    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<ShareCodeResponseDto> completeGame(@PathVariable Long sessionId) {
        String shareCode = gamePlayService.completeGameAndGetShareCode(sessionId);
        return ResponseEntity.ok(new ShareCodeResponseDto(shareCode));
    }

    /**
     * 게임 결과 조회
     */
    @GetMapping("/sessions/{sessionId}/results")
    public ResponseEntity<GameResultDto> getGameResults(@PathVariable Long sessionId) {
        GameResultDto results = gamePlayService.getGameResults(sessionId);
        return ResponseEntity.ok(results);
    }

    /**
     * 공유 코드로 세션 정보 조회
     */
    @GetMapping("/share/{shareCode}")
    public ResponseEntity<GameSessionResponseDto> getSessionByShareCode(@PathVariable String shareCode) {
        GameSessionResponseDto session = gamePlayService.getSessionByShareCode(shareCode);
        return ResponseEntity.ok(session);
    }

    /**
     * 결과 비교
     */
    @PostMapping("/compare")
    public ResponseEntity<GameComparisonDto> compareResults(
            @RequestParam String shareCode,
            @RequestParam Long compareSessionId) {
        GameComparisonDto comparison = gamePlayService.compareResults(shareCode, compareSessionId);
        return ResponseEntity.ok(comparison);
    }

    /**
     * 공유 코드 검증
     */
    @GetMapping("/share/{shareCode}/validate")
    public ResponseEntity<Boolean> validateShareCode(@PathVariable String shareCode) {
        try {
            gamePlayService.getSessionByShareCode(shareCode);
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(false);
        }
    }
}
