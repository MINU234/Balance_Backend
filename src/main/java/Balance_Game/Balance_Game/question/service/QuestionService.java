// src/main/java/Balance_Game/Balance_Game/service/QuestionService.java
package Balance_Game.Balance_Game.question.service;

import Balance_Game.Balance_Game.question.dto.PopularQuestionDto;
import Balance_Game.Balance_Game.question.dto.QuestionCreateRequestDto; // 새로 추가할 DTO
import Balance_Game.Balance_Game.question.dto.QuestionDto;
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
     * 인기 질문 목록 페이징 조회 (승인된 질문만)
     */
    @Cacheable(value = "popularQuestions", key = "#pageable.pageNumber", cacheManager = "cacheManager")
    @Transactional(readOnly = true)
    public Page<PopularQuestionDto> getPopularQuestions(Pageable pageable) {
        // 승인된 질문만 조회하도록 수정
        Page<Question> popularQuestionsPage = questionRepository.findApprovedPopularQuestions(pageable);
        return popularQuestionsPage.map(this::convertToPopularQuestionDto);
    }

    /**
     * 새로운 질문 생성 (승인 대기 상태로 생성)
     */
    @Transactional
    public Long createQuestion(QuestionCreateRequestDto requestDto, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 질문 생성 - 기본적으로 PENDING 상태로 생성됨
        Question question = Question.builder()
                .text(requestDto.getText())
                .optionAText(requestDto.getOptionAText())
                .optionBText(requestDto.getOptionBText())
                .optionAImageUrl(requestDto.getOptionAImageUrl())
                .optionBImageUrl(requestDto.getOptionBImageUrl())
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

    @Transactional(readOnly = true)
    public Page<QuestionDto> findByCreatorEmail(String email, Pageable pageable) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Question> questions = questionRepository.findByCreator(creator, pageable);

        return questions.map(QuestionDto::from);
    }
}
