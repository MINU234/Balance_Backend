// StockImageRepository.java (신규)
package Balance_Game.Balance_Game.repository;

import Balance_Game.Balance_Game.entity.StockImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockImageRepository extends JpaRepository<StockImage, Long> {
    // 태그를 포함하는 이미지 목록을 페이징하여 조회
    Page<StockImage> findByTagsContaining(String tag, Pageable pageable);
}
