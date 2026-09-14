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
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Job> createJob(@Valid @RequestBody JobRequest request, Authentication auth) {
        Long recruiterId = (Long) auth.getDetails();
        Job job = jobService.createJob(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<Job>> getMyJobs(Authentication auth) {
        Long recruiterId = (Long) auth.getDetails();
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