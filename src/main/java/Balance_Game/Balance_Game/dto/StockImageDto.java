package Balance_Game.Balance_Game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StockImageDto {
    private Long id;
    private String imageUrl;
    private String description;
}