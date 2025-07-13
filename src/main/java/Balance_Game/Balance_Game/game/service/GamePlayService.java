// src/main/java/Balance_Game/Balance_Game/service/GamePlayService.java
package Balance_Game.Balance_Game.game.service;

import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.game.entity.SelectedOption;
import Balance_Game.Balance_Game.game.entity.UserAnswer;
import Balance_Game.Balance_Game.game.repository.GameSessionRepository;
import Balance_Game.Balance_Game.game.repository.UserAnswerRepository;
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import Balance_Game.Balance_Game.question.entity.QuestionBundleStats;
import Balance_Game.Balance_Game.question.entity.QuestionStats;
import Balance_Game.Balance_Game.question.repository.QuestionBundleRepository;
import Balance_Game.Balance_Game.question.repository.QuestionBundleStatsRepository;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.question.repository.QuestionStatsRepository;
import Balance_Game.Balance_Game.game.dto.AnswerRequestDto;
import Balance_Game.Balance_Game.game.dto.GameSessionResponseDto;
import Balance_Game.Balance_Game.game.dto.StartGameRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GamePlayService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionBundleRepository questionBundleRepository;
    private final QuestionBundleStatsRepository questionBundleStatsRepository;
    private final QuestionStatsRepository questionStatsRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public GameSessionResponseDto startGame(StartGameRequestDto requestDto) {
        // 1. 요청된 ID로 QuestionBundle을 찾습니다. 없으면 예외를 발생시킵니다.
        QuestionBundle bundle = questionBundleRepository.findById(requestDto.getBundleId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 질문 묶음을 찾을 수 없습니다: " + requestDto.getBundleId()));

        // 2. 새로운 GameSession 엔티티를 생성합니다.
        GameSession newSession = GameSession.builder()
                .questionBundle(bundle)
                .sessionStatus("IN_PROGRESS") // 세션 상태를 '진행 중'으로 초기화
                .build();
        GameSession savedSession = gameSessionRepository.save(newSession);

        // 3. 해당 질문 묶음의 통계(QuestionBundleStats)를 찾아 playCount를 1 증가시킵니다.
        QuestionBundleStats bundleStats = questionBundleStatsRepository.findById(bundle.getId())
                .orElseThrow(() -> new EntityNotFoundException("데이터 무결성 오류: 해당 질문 묶음의 통계 정보가 없습니다. ID: " + bundle.getId()));
        bundleStats.incrementPlayCount();

        // 4. 생성된 세션의 ID를 클라이언트에게 반환합니다.
        return new GameSessionResponseDto(savedSession.getId());
    }

    /**
     * 사용자의 답변을 기록하고, 관련 통계를 업데이트합니다.
     * @param requestDto 세션 ID, 질문 ID, 사용자의 선택지("A" 또는 "B")를 포함한 DTO
     */
    @Transactional
    public void recordAnswer(AnswerRequestDto requestDto) {
        // 1. 관련 엔티티 조회 (기존과 동일)
        GameSession session = gameSessionRepository.findById(requestDto.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 게임 세션 ID입니다: " + requestDto.getSessionId()));

        Question question = questionRepository.findById(requestDto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 질문을 찾을 수 없습니다: " + requestDto.getQuestionId()));

        // 2. [핵심 변경사항 ①] DTO의 String을 SelectedOption Enum으로 변환
        SelectedOption selectedEnumOption;
        try {
            // "a" 또는 "b" 같은 소문자 입력에도 대응하기 위해 대문자로 변경
            selectedEnumOption = SelectedOption.valueOf(requestDto.getSelectedOption().toUpperCase());
        } catch (IllegalArgumentException e) {
            // "A" 또는 "B"가 아닌 다른 값이 들어오면 즉시 에러 발생
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택지는 'A' 또는 'B'여야 합니다.");
        }

        // 3. [핵심 변경사항 ②] UserAnswer 엔티티를 생성할 때 변환된 'enum' 사용
        UserAnswer userAnswer = UserAnswer.builder()
                .gameSession(session)
                .question(question)
                .selectedOption(selectedEnumOption) // String이 아닌 enum을 전달
                .build();
        userAnswerRepository.save(userAnswer);

        // 4. [핵심 변경사항 ③] QuestionStats를 업데이트할 때도 변환된 'enum' 사용
        QuestionStats questionStats = questionStatsRepository.findById(requestDto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("데이터 무결성 오류: 해당 질문의 통계 정보가 없습니다. ID: " + requestDto.getQuestionId()));

        questionStats.incrementCount(selectedEnumOption); // String이 아닌 enum을 전달
    }
}
