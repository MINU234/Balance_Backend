package Balance_Game.Balance_Game.image.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AzureUploadService {


    private final BlobServiceClient blobServiceClient;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    // 이 메서드의 역할: MultipartFile을 받아 Azure Blob Storage에 업로드하고, 결과 URL을 반환한다.
    public String upload(MultipartFile multipartFile) throws IOException {
        // 1. 파일을 저장할 컨테이너 클라이언트 가져오기
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        if (!containerClient.exists()) {
            containerClient.create();
        }

        // 2. 파일 이름 중복 방지를 위해 UUID 사용
        String blobName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // 3. 스트림을 통해 파일 업로드
        blobClient.upload(multipartFile.getInputStream(), multipartFile.getSize(), true);

        // 4. 업로드된 파일의 URL 반환
        return blobClient.getBlobUrl();
    }
}
