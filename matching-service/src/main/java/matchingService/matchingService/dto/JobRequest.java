package matchingService.matchingService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobRequest {
    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Job requirements are required")
    private String requirements;
}