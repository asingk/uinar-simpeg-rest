package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PemutihanCreateRequest {
    @NotNull
    private LocalDate tanggal;
    @NotBlank
    private String status;
}
