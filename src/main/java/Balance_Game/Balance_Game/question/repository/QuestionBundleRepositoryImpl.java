// src/main/java/Balance_Game/Balance_Game/repository/QuestionBundleRepositoryImpl.java
package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.question.dto.PopularBundleDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static Balance_Game.Balance_Game.entity.QQuestionBundle.questionBundle;
import static Balance_Game.Balance_Game.entity.QQuestionBundleStats.questionBundleStats;
import static Balance_Game.Balance_Game.entity.QUser.user;

@RequiredArgsConstructor
public class QuestionBundleRepositoryImpl implements QuestionBundleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PopularBundleDto> findPopularBundles(Pageable pageable) {
        // 1. 데이터 목록 조회 쿼리
        List<PopularBundleDto> content = queryFactory
                .select(Projections.constructor(PopularBundleDto.class,
                        questionBundle.id,
                        questionBundle.title,
                        questionBundle.description,
                        user.nickname,
                        questionBundleStats.playCount,
                        questionBundle.bundleQuestions.size(),
                        questionBundle.keywords
                ))
                .from(questionBundle)
                .join(questionBundle.creator, user)
                .leftJoin(questionBundle.stats, questionBundleStats)
                .where(questionBundle.isPublic.isTrue())
                .orderBy(questionBundleStats.playCount.desc().nullsLast(), questionBundle.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 카운트 조회 쿼리 (페이징을 위해 별도로 실행)
        Long total = queryFactory
                .select(questionBundle.count())
                .from(questionBundle)
                .where(questionBundle.isPublic.isTrue())
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
