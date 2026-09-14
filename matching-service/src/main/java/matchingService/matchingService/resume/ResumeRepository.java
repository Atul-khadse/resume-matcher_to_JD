package matchingService.matchingService.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findTopByCandidateIdOrderByUploadedAtDesc(Long candidateId);
    List<Resume> findByCandidateId(Long candidateId);
}