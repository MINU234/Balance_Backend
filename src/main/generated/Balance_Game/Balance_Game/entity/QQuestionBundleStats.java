package Balance_Game.Balance_Game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import Balance_Game.Balance_Game.question.entity.QuestionBundleStats;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionBundleStats is a Querydsl query type for QuestionBundleStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionBundleStats extends EntityPathBase<QuestionBundleStats> {

    private static final long serialVersionUID = 1636282220L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionBundleStats questionBundleStats = new QQuestionBundleStats("questionBundleStats");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> playCount = createNumber("playCount", Long.class);

    public final QQuestionBundle questionBundle;

    public QQuestionBundleStats(String variable) {
        this(QuestionBundleStats.class, forVariable(variable), INITS);
    }

    public QQuestionBundleStats(Path<? extends QuestionBundleStats> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionBundleStats(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionBundleStats(PathMetadata metadata, PathInits inits) {
        this(QuestionBundleStats.class, metadata, inits);
    }

    public QQuestionBundleStats(Class<? extends QuestionBundleStats> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.questionBundle = inits.isInitialized("questionBundle") ? new QQuestionBundle(forProperty("questionBundle"), inits.get("questionBundle")) : null;
    }

}

