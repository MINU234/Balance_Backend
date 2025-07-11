package Balance_Game.Balance_Game.service;

import Balance_Game.Balance_Game.dto.StockImageDto;
import Balance_Game.Balance_Game.entity.StockImage;
import Balance_Game.Balance_Game.repository.StockImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자가 미리 등록한 기본 이미지(Stock Image)를 조회하는 로직을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 이 서비스는 데이터를 읽기만 하므로, readOnly = true로 설정하여 성능을 최적화합니다.
public class StockImageService {

    // 데이터베이스와 통신하기 위한 Repository
    private final StockImageRepository stockImageRepository;

    /**
     * 기본 이미지 목록을 조건에 따라 페이징하여 조회합니다.
     * @param tag (선택) 클라이언트가 검색한 이미지 태그. null이거나 비어있을 수 있습니다.
     * @param pageable 페이징 정보 (페이지 번호, 페이지 당 항목 수)
     * @return 페이징된 이미지 DTO 목록
     */
    public Page<StockImageDto> findStockImages(String tag, Pageable pageable) {
        Page<StockImage> images;

        // 1. 태그(검색어)가 있는지 없는지 확인합니다.
        if (tag != null && !tag.isEmpty()) {
            // 태그가 있으면, tags 필드에 해당 태그를 포함하는 이미지를 검색합니다.
            images = stockImageRepository.findByTagsContaining(tag, pageable);
        } else {
            // 태그가 없으면, 모든 이미지를 조회합니다.
            images = stockImageRepository.findAll(pageable);
        }

        // 2. 조회된 Entity(StockImage) 목록을 DTO(StockImageDto) 목록으로 변환하여 반환합니다.
        //    이렇게 하면 데이터베이스 구조가 프론트엔드에 직접 노출되는 것을 방지할 수 있습니다.
        return images.map(image -> StockImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .description(image.getDescription())
                .build());
    }
}
