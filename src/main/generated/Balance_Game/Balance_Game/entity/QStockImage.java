package Balance_Game.Balance_Game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import Balance_Game.Balance_Game.image.entity.StockImage;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStockImage is a Querydsl query type for StockImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStockImage extends EntityPathBase<StockImage> {

    private static final long serialVersionUID = 1124195920L;

    public static final QStockImage stockImage = new QStockImage("stockImage");

    public final QBaseTimeEntity _super = new QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath tags = createString("tags");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStockImage(String variable) {
        super(StockImage.class, forVariable(variable));
    }

    public QStockImage(Path<? extends StockImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStockImage(PathMetadata metadata) {
        super(StockImage.class, metadata);
    }

}

