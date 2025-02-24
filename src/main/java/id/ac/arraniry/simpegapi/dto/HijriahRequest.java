package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HijriahRequest {
    @NotNull
    private LocalDate awalRamadhan;
    @NotNull
    private LocalDate awalSyawal;
    @NotNull
    private Integer tahun;
}
