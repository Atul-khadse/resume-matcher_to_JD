package matchingService.matchingService.job;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import matchingService.matchingService.dto.JobRequest;
import matchingService.matchingService.matching.AsyncMatchingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final AsyncMatchingService asyncMatchingService;

    public Job createJob(Long recruiterId, JobRequest req) {
        Job job = Job.builder()
                .recruiterId(recruiterId)
                .title(req.getTitle())
                .company(req.getCompany())
                .description(req.getDescription())
                .requirements(req.getRequirements())
                .build();
        Job saved = jobRepository.save(job);
        // Async background recalculation against existing resumes
       try {
        asyncMatchingService.scoreJobAgainstAllResumes(saved.getId());
    } catch (Exception e) {
           log.error("Failed to start background scoring for job id {}: {}", saved.getId(), e.getMessage());
    }
        return saved;
    }

    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));
    }
}