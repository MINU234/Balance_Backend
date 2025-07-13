package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryCustom {
    // 인기 질문 목록 조회 (구현은 Impl 클래스에서)
    Page<Question> findPopularQuestions(Pageable pageable);
}
