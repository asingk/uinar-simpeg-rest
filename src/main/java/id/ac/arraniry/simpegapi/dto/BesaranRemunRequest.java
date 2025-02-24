package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BesaranRemunRequest {
    @NotNull
    private Integer persen;
    @NotBlank
    private String updatedBy;
}
