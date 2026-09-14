package matchingService.matchingService.job;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchingService.matchingService.dto.JobRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(
            @Valid @RequestBody JobRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long recruiterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (recruiterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing user identity header from Gateway");
        }

        // Validate role forwarded from Gateway JWT claims
        if (!"RECRUITER".equalsIgnoreCase(role) && !"ROLE_RECRUITER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only recruiters can post jobs");
        }

        Job job = jobService.createJob(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyJobs(@RequestHeader(value = "X-User-Id", required = false) Long recruiterId) {
        if (recruiterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing user identity header from Gateway");
        }
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }
}