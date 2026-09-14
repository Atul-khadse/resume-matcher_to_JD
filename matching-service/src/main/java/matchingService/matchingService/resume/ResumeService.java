package matchingService.matchingService.resume;

import lombok.RequiredArgsConstructor;
import matchingService.matchingService.matching.AsyncMatchingService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final AsyncMatchingService asyncMatchingService;

    public Map<String, Object> uploadResume(Long candidateId, String email, String fullName, MultipartFile file) throws IOException {
        String extractedText = pdfTextExtractor.extractText(file);

        Resume resume = resumeRepository.findTopByCandidateIdOrderByUploadedAtDesc(candidateId)
                .orElse(Resume.builder()
                        .candidateId(candidateId)
                        .candidateEmail(email)
                        .candidateName(fullName)
                        .build());

        resume.setFileName(file.getOriginalFilename());
        resume.setExtractedText(extractedText);

        Resume saved = resumeRepository.save(resume);

        // Async scoring dispatched immediately: response thread does not block on scoring
        asyncMatchingService.scoreResumeAgainstAllJobs(saved.getId());

        return Map.of(
                "message", "Resume uploaded and processed successfully. Scoring started asynchronously.",
                "resumeId", saved.getId(),
                "fileName", saved.getFileName(),
                "wordCount", extractedText.split("\\s+").length
        );
    }

    public Resume getCandidateResume(Long candidateId) {
        return resumeRepository.findTopByCandidateIdOrderByUploadedAtDesc(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("No resume found for candidate"));
    }
}