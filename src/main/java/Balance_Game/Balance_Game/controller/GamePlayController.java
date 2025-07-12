// src/main/java/Balance_Game/Balance_Game/controller/GamePlayController.java
package Balance_Game.Balance_Game.controller;

import Balance_Game.Balance_Game.service.GamePlayService;
import Balance_Game.Balance_Game.dto.AnswerRequestDto;
import Balance_Game.Balance_Game.dto.GameSessionResponseDto;
import Balance_Game.Balance_Game.dto.StartGameRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GamePlayController {

    private final GamePlayService gamePlayService;

    @PostMapping("/start")
    public ResponseEntity<GameSessionResponseDto> startGame(@Valid @RequestBody StartGameRequestDto requestDto) {
        GameSessionResponseDto response = gamePlayService.startGame(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/answer")
    public ResponseEntity<Void> submitAnswer(@Valid @RequestBody AnswerRequestDto answerRequestDto) {
        gamePlayService.recordAnswer(answerRequestDto);
        return ResponseEntity.ok().build();
    }
}
