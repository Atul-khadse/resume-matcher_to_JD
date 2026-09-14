package matchingService.matchingService.job;

import lombok.RequiredArgsConstructor;
import matchingService.matchingService.dto.JobRequest;
import matchingService.matchingService.matching.AsyncMatchingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
        asyncMatchingService.scoreJobAgainstAllResumes(saved.getId());
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