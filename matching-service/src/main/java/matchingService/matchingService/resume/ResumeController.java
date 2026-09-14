package matchingService.matchingService.resume;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @Value("${jwt.secret}")
    private String secret;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-User-Id", required = false) Long candidateId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (candidateId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing user identity header"));
        }

        if (!"CANDIDATE".equalsIgnoreCase(role) && !"ROLE_CANDIDATE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Only candidates can upload resumes"));
        }

        if (file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload a valid PDF document"));
        }

        try {
            // Optional name fallback if present in token
            String fullName = "Candidate";
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                    String nameClaim = claims.get("fullName", String.class);
                    if (nameClaim != null && !nameClaim.isBlank()) {
                        fullName = nameClaim;
                    }
                } catch (Exception ignored) {
                    // Fall back to candidate email/default if optional parse fails
                }
            }

            Map<String, Object> result = resumeService.uploadResume(candidateId, email, fullName, file);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to extract/save resume: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyResume(
            @RequestHeader(value = "X-User-Id", required = false) Long candidateId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (candidateId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing user identity header"));
        }

        if (!"CANDIDATE".equalsIgnoreCase(role) && !"ROLE_CANDIDATE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Only candidates can view their resume"));
        }

        try {
            return ResponseEntity.ok(resumeService.getCandidateResume(candidateId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}