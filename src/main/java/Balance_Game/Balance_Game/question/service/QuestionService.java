// src/main/java/Balance_Game/Balance_Game/service/QuestionService.java
package Balance_Game.Balance_Game.question.service;

import Balance_Game.Balance_Game.question.dto.PopularQuestionDto;
import Balance_Game.Balance_Game.question.dto.QuestionCreateRequestDto; // 새로 추가할 DTO
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.question.entity.QuestionStats;
import Balance_Game.Balance_Game.user.entity.User;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.question.repository.QuestionStatsRepository;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionStatsRepository questionStatsRepository;
    private final UserRepository userRepository;

    /**
     * 인기 질문 목록 페이징 조회 (기존 기능)
     */
    @Cacheable(value = "popularQuestions", key = "#pageable.pageNumber", cacheManager = "cacheManager")
    @Transactional(readOnly = true)
    public Page<PopularQuestionDto> getPopularQuestions(Pageable pageable) {
        // 리포지토리의 findPopularQuestions 메서드에 pageable 객체를 그대로 전달합니다.
        Page<Question> popularQuestionsPage = questionRepository.findPopularQuestions(pageable);

        return popularQuestionsPage.map(this::convertToPopularQuestionDto);
    }

    /**
     * 새로운 질문 생성 (기능 추가)
     */
    @Transactional
    public Long createQuestion(QuestionCreateRequestDto requestDto, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // DTO에 담겨 온 URL을 사용하여 Question 객체를 생성합니다.
        Question question = Question.builder()
                .text(requestDto.getText())
                .optionAText(requestDto.getOptionAText())
                .optionBText(requestDto.getOptionBText())
                .optionAImageUrl(requestDto.getOptionAImageUrl()) // 전달받은 URL을 그대로 사용
                .optionBImageUrl(requestDto.getOptionBImageUrl()) // 전달받은 URL을 그대로 사용
                .creator(creator)
                .keyword(requestDto.getKeyword())
                .build();

        Question savedQuestion = questionRepository.save(question);

        // 통계 엔티티 생성
        QuestionStats stats = QuestionStats.builder().question(savedQuestion).build();
        questionStatsRepository.save(stats);

        return savedQuestion.getId();
    }

    // 엔티티를 DTO로 변환하는 헬퍼 메서드
    private PopularQuestionDto convertToPopularQuestionDto(Question question) {
        String creatorNickname = (question.getCreator() != null) ? question.getCreator().getNickname() : "관리자";
        return PopularQuestionDto.builder()
                .id(question.getId())
                .text(question.getText())
                .optionAText(question.getOptionAText())
                .optionBText(question.getOptionBText())
                .creatorNickname(creatorNickname)
                .build();
    }
}
