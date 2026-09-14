package matchingService.matchingService.matching;

import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<MatchScore>> getMatchesForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(matchRepository.findByJobIdOrderByScorePercentageDesc(jobId));
    }

    @GetMapping("/my-scores")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<MatchScore>> getCandidateMatches(Authentication auth) {
        Long candidateId = (Long) auth.getDetails();
        return ResponseEntity.ok(matchRepository.findByResumeCandidateIdOrderByScorePercentageDesc(candidateId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "matching-service"));
    }
}