package matchingService.matchingService.matching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchingService.matchingService.job.Job;
import matchingService.matchingService.job.JobRepository;
import matchingService.matchingService.resume.Resume;
import matchingService.matchingService.resume.ResumeRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMatchingService {

    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final MatchRepository matchRepository;
    private final TfIdfScorer scorer;

    @Async
    @Transactional
    public void scoreResumeAgainstAllJobs(Long resumeId) {
        log.info("Starting async scoring for Resume ID: {}", resumeId);
        Resume resume = resumeRepository.findById(resumeId).orElse(null);
        if (resume == null) return;

        List<Job> jobs = jobRepository.findAll();
        for (Job job : jobs) {
            scorePair(job, resume);
        }
        log.info("Completed async scoring for Resume ID: {} against {} jobs", resumeId, jobs.size());
    }

    @Async
    @Transactional
    public void scoreJobAgainstAllResumes(Long jobId) {
        log.info("Starting async scoring for Job ID: {}", jobId);
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        List<Resume> resumes = resumeRepository.findAll();
        for (Resume resume : resumes) {
            scorePair(job, resume);
        }
        log.info("Completed async scoring for Job ID: {} against {} resumes", jobId, resumes.size());
    }
    
private void scorePair(Job job, Resume resume) {
    // Put requirements first and give them double presence so technical skills dominate the vector
    String requirements = job.getRequirements() != null ? job.getRequirements() : "";
    String jobFullText = requirements + " " + requirements + " " + job.getTitle() + " " + job.getDescription();

    TfIdfScorer.MatchResult result = scorer.calculateScore(jobFullText, resume.getExtractedText());

    MatchScore match = matchRepository.findByJobIdAndResumeId(job.getId(), resume.getId())
            .orElse(MatchScore.builder().job(job).resume(resume).build());

    match.setScorePercentage(result.scorePercentage());
    match.setMatchedKeywords(String.join(", ", result.topKeywords()));
    matchRepository.save(match);
}
}