package Balance_Game.Balance_Game.game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAnswer is a Querydsl query type for UserAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAnswer extends EntityPathBase<UserAnswer> {

    private static final long serialVersionUID = 1507597336L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAnswer userAnswer = new QUserAnswer("userAnswer");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final QGameSession gameSession;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final Balance_Game.Balance_Game.question.entity.QQuestion question;

    public final EnumPath<SelectedOption> selectedOption = createEnum("selectedOption", SelectedOption.class);

    public final Balance_Game.Balance_Game.user.entity.QUser user;

    public QUserAnswer(String variable) {
        this(UserAnswer.class, forVariable(variable), INITS);
    }

    public QUserAnswer(Path<? extends UserAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAnswer(PathMetadata metadata, PathInits inits) {
        this(UserAnswer.class, metadata, inits);
    }

    public QUserAnswer(Class<? extends UserAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gameSession = inits.isInitialized("gameSession") ? new QGameSession(forProperty("gameSession"), inits.get("gameSession")) : null;
        this.question = inits.isInitialized("question") ? new Balance_Game.Balance_Game.question.entity.QQuestion(forProperty("question"), inits.get("question")) : null;
        this.user = inits.isInitialized("user") ? new Balance_Game.Balance_Game.user.entity.QUser(forProperty("user")) : null;
    }

}

