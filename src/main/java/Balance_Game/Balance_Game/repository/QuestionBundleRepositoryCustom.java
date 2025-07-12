package Balance_Game.Balance_Game.repository;

import Balance_Game.Balance_Game.dto.PopularBundleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionBundleRepositoryCustom {
    Page<PopularBundleDto> findPopularBundles(Pageable pageable);
}
