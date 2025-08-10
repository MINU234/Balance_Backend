// src/main/java/Balance_Game/Balance_Game/repository/QuestionRepositoryImpl.java
package Balance_Game.Balance_Game.question.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import Balance_Game.Balance_Game.question.entity.ApprovalStatus;
import Balance_Game.Balance_Game.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

import static Balance_Game.Balance_Game.question.entity.QQuestion.question;
import static Balance_Game.Balance_Game.question.entity.QQuestionStats.questionStats;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Question> findPopularQuestions(Pageable pageable) {
        // 1. 실제 데이터 조회 쿼리 (페이징 적용)
        List<Question> content = queryFactory
                .selectFrom(question)
                .join(questionStats).on(question.id.eq(questionStats.id))
                .orderBy(questionStats.totalCount.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 카운트 조회 쿼리
        Long total = queryFactory
                .select(question.count())
                .from(question)
                .join(questionStats).on(question.id.eq(questionStats.id))
                .fetchOne();

        // 3. Page 객체로 만들어서 반환
        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public Page<Question> findApprovedPopularQuestions(Pageable pageable) {
        // 1. 승인된 질문만 조회 (페이징 적용)
        List<Question> content = queryFactory
                .selectFrom(question)
                .join(questionStats).on(question.id.eq(questionStats.id))
                .where(
                    question.approvalStatus.eq(ApprovalStatus.APPROVED),
                    question.isActive.isTrue()
                )
                .orderBy(questionStats.totalCount.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 승인된 질문의 전체 카운트 조회
        Long total = queryFactory
                .select(question.count())
                .from(question)
                .join(questionStats).on(question.id.eq(questionStats.id))
                .where(
                    question.approvalStatus.eq(ApprovalStatus.APPROVED),
                    question.isActive.isTrue()
                )
                .fetchOne();

        // 3. Page 객체로 만들어서 반환
        return new PageImpl<>(content, pageable, total);
    }
}
