package com.example.demo.web;

import com.example.demo.domain.Submission;
import com.example.demo.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class FileController {

    // ▼▼▼ [추가] SubmissionRepository 주입 ▼▼▼
    private final SubmissionRepository submissionRepo;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // [수정] URL의 originalFileName은 화면에 표시하기 위한 용도로만 사용
    @GetMapping("/download/{submissionId}/{originalFileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long submissionId,
            @PathVariable String originalFileName) throws MalformedURLException {

        // 1. submissionId로 데이터베이스에서 제출 정보를 조회합니다.
        Submission submission = submissionRepo.findById(submissionId)
                .orElse(null);

        // 제출 정보가 없거나 파일 경로가 없으면 404를 반환합니다.
        if (submission == null || submission.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. DB에 저장된 실제 파일명(UUID)을 사용하여 파일의 전체 경로를 찾습니다.
        String savedFileName = submission.getFilePath();
        Path filePath = Paths.get(uploadDir).resolve(savedFileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        // 파일이 존재하지 않으면 404를 반환합니다.
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // 3. 다운로드 시에는 DB에 저장된 '원본 파일명'을 사용합니다.
        String downloadFileName = submission.getOriginalFileName();
        String encodedFileName = UriUtils.encode(downloadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}