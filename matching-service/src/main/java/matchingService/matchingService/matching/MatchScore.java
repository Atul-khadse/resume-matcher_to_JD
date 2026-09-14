package matchingService.matchingService.matching;

import jakarta.persistence.*;
import lombok.*;
import matchingService.matchingService.job.Job;
import matchingService.matchingService.resume.Resume;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "resume_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "score_percentage", nullable = false)
    private Double scorePercentage;

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(name = "computed_at")
    private LocalDateTime computedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.computedAt = LocalDateTime.now();
    }
}