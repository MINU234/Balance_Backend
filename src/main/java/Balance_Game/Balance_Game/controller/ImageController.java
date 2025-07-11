package Balance_Game.Balance_Game.controller;

import Balance_Game.Balance_Game.dto.StockImageDto;
import Balance_Game.Balance_Game.service.AzureUploadService;
import Balance_Game.Balance_Game.service.StockImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    // Azure에 실제 파일 업로드를 담당하는 서비스
    private final AzureUploadService azureUploadService;

    // 관리자가 미리 등록한 기본 이미지를 조회하는 서비스
    private final StockImageService stockImageService;

    /**
     * 사용자가 직접 이미지를 업로드하는 API
     * @param image 클라이언트에서 'image'라는 이름으로 보낸 이미지 파일
     * @return 업로드 완료 후, 이미지에 접근할 수 있는 전체 URL
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
        // 이제 파일만 전달하면 됩니다.
        String imageUrl = azureUploadService.upload(image);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * 관리자가 제공하는 기본 이미지 목록을 조회하는 API
     * @param tag (선택) 검색할 이미지 태그
     * @param pageable 페이징 정보 (예: page=0&size=20)
     * @return 페이징된 이미지 정보 목록
     */
    @GetMapping("/stock")
    public ResponseEntity<Page<StockImageDto>> getStockImages(
            @RequestParam(required = false) String tag,
            Pageable pageable) {

        Page<StockImageDto> stockImages = stockImageService.findStockImages(tag, pageable);
        return ResponseEntity.ok(stockImages);
    }
}
