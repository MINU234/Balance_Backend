package Balance_Game.Balance_Game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import Balance_Game.Balance_Game.question.entity.BundleQuestion;
import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionBundle is a Querydsl query type for QuestionBundle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionBundle extends EntityPathBase<QuestionBundle> {

    private static final long serialVersionUID = -1544272653L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionBundle questionBundle = new QQuestionBundle("questionBundle");

    public final QBaseTimeEntity _super = new QBaseTimeEntity(this);

    public final ListPath<BundleQuestion, QBundleQuestion> bundleQuestions = this.<BundleQuestion, QBundleQuestion>createList("bundleQuestions", BundleQuestion.class, QBundleQuestion.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QUser creator;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPublic = createBoolean("isPublic");

    public final StringPath keywords = createString("keywords");

    public final QQuestionBundleStats stats;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QQuestionBundle(String variable) {
        this(QuestionBundle.class, forVariable(variable), INITS);
    }

    public QQuestionBundle(Path<? extends QuestionBundle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionBundle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionBundle(PathMetadata metadata, PathInits inits) {
        this(QuestionBundle.class, metadata, inits);
    }

    public QQuestionBundle(Class<? extends QuestionBundle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.creator = inits.isInitialized("creator") ? new QUser(forProperty("creator")) : null;
        this.stats = inits.isInitialized("stats") ? new QQuestionBundleStats(forProperty("stats"), inits.get("stats")) : null;
    }

}

