// Balance_Game.Balance_Game.service 패키지에 생성
package Balance_Game.Balance_Game.service;

import Balance_Game.Balance_Game.dto.AnswerRequestDto;
import Balance_Game.Balance_Game.entity.*;
import Balance_Game.Balance_Game.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionStatsRepository questionStatsRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public void submitAnswer(AnswerRequestDto requestDto, String userEmail) {
        // 1. 사용자 및 질문 엔티티 조회
        // 비회원 플레이를 지원하려면 userEmail이 null일 수 있으므로 분기 처리
        User user = (userEmail != null) ? userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")) : null;

        Question question = questionRepository.findById(requestDto.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        // 2. 답변 기록(UserAnswer) 저장
        UserAnswer userAnswer = UserAnswer.builder()
                .user(user)
                .question(question)
                .selectedOption(SelectedOption.valueOf(requestDto.getSelectedOption()))
                .build();
        userAnswerRepository.save(userAnswer);

        // 3. 통계(QuestionStats) 업데이트
        QuestionStats stats = questionStatsRepository.findById(question.getId())
                .orElseThrow(() -> new IllegalStateException("질문에 대한 통계 정보가 없습니다."));

        stats.incrementCount(requestDto.getSelectedOption()); // 통계 증가 로직 호출
    }
}
