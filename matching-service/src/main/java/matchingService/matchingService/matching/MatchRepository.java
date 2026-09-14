package matchingService.matchingService.matching;

import matchingService.matchingService.matching.MatchScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<MatchScore, Long> {
    List<MatchScore> findByJobIdOrderByScorePercentageDesc(Long jobId);
    List<MatchScore> findByResumeCandidateIdOrderByScorePercentageDesc(Long candidateId);
    Optional<MatchScore> findByJobIdAndResumeId(Long jobId, Long resumeId);
}