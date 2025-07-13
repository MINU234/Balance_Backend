package Balance_Game.Balance_Game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.game.entity.UserAnswer;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGameSession is a Querydsl query type for GameSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGameSession extends EntityPathBase<GameSession> {

    private static final long serialVersionUID = -489256647L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGameSession gameSession = new QGameSession("gameSession");

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser player1;

    public final QUser player2;

    public final QQuestionBundle questionBundle;

    public final StringPath sessionStatus = createString("sessionStatus");

    public final ListPath<UserAnswer, QUserAnswer> userAnswers = this.<UserAnswer, QUserAnswer>createList("userAnswers", UserAnswer.class, QUserAnswer.class, PathInits.DIRECT2);

    public QGameSession(String variable) {
        this(GameSession.class, forVariable(variable), INITS);
    }

    public QGameSession(Path<? extends GameSession> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGameSession(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGameSession(PathMetadata metadata, PathInits inits) {
        this(GameSession.class, metadata, inits);
    }

    public QGameSession(Class<? extends GameSession> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.player1 = inits.isInitialized("player1") ? new QUser(forProperty("player1")) : null;
        this.player2 = inits.isInitialized("player2") ? new QUser(forProperty("player2")) : null;
        this.questionBundle = inits.isInitialized("questionBundle") ? new QQuestionBundle(forProperty("questionBundle"), inits.get("questionBundle")) : null;
    }

}

