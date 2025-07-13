package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.question.dto.PopularBundleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionBundleRepositoryCustom {
    Page<PopularBundleDto> findPopularBundles(Pageable pageable);
}
