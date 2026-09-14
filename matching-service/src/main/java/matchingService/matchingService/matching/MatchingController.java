package matchingService.matchingService.matching;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchRepository matchRepository;

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getMatchesForJob(
            @PathVariable Long jobId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (!"RECRUITER".equalsIgnoreCase(role) && !"ROLE_RECRUITER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Only recruiters can view candidate matches"));
        }

        return ResponseEntity.ok(matchRepository.findByJobIdOrderByScorePercentageDesc(jobId));
    }

    @GetMapping("/my-scores")
    public ResponseEntity<?> getCandidateMatches(
            @RequestHeader(value = "X-User-Id", required = false) Long candidateId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (candidateId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing user identity header"));
        }

        if (!"CANDIDATE".equalsIgnoreCase(role) && !"ROLE_CANDIDATE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Only candidates can view scores"));
        }

        return ResponseEntity.ok(matchRepository.findByResumeCandidateIdOrderByScorePercentageDesc(candidateId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "matching-service"));
    }
}