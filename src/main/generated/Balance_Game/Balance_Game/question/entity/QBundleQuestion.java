package Balance_Game.Balance_Game.question.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBundleQuestion is a Querydsl query type for BundleQuestion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBundleQuestion extends EntityPathBase<BundleQuestion> {

    private static final long serialVersionUID = -549390165L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBundleQuestion bundleQuestion = new QBundleQuestion("bundleQuestion");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> orderIndex = createNumber("orderIndex", Integer.class);

    public final QQuestion question;

    public final QQuestionBundle questionBundle;

    public QBundleQuestion(String variable) {
        this(BundleQuestion.class, forVariable(variable), INITS);
    }

    public QBundleQuestion(Path<? extends BundleQuestion> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBundleQuestion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBundleQuestion(PathMetadata metadata, PathInits inits) {
        this(BundleQuestion.class, metadata, inits);
    }

    public QBundleQuestion(Class<? extends BundleQuestion> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.question = inits.isInitialized("question") ? new QQuestion(forProperty("question"), inits.get("question")) : null;
        this.questionBundle = inits.isInitialized("questionBundle") ? new QQuestionBundle(forProperty("questionBundle"), inits.get("questionBundle")) : null;
    }

}

