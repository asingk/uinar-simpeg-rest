package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HariLiburRequest {
    @NotNull
    LocalDate tanggal;
}
