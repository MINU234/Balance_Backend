package Balance_Game.Balance_Game.question.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionStats is a Querydsl query type for QuestionStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionStats extends EntityPathBase<QuestionStats> {

    private static final long serialVersionUID = -260768298L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionStats questionStats = new QQuestionStats("questionStats");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> optionACount = createNumber("optionACount", Long.class);

    public final NumberPath<Long> optionBCount = createNumber("optionBCount", Long.class);

    public final QQuestion question;

    public final NumberPath<Long> totalCount = createNumber("totalCount", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QQuestionStats(String variable) {
        this(QuestionStats.class, forVariable(variable), INITS);
    }

    public QQuestionStats(Path<? extends QuestionStats> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionStats(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionStats(PathMetadata metadata, PathInits inits) {
        this(QuestionStats.class, metadata, inits);
    }

    public QQuestionStats(Class<? extends QuestionStats> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.question = inits.isInitialized("question") ? new QQuestion(forProperty("question"), inits.get("question")) : null;
    }

}

