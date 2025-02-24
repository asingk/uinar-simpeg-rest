package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class JamKerjaRequest {
    @NotNull
    private LocalTime jamDatangStart;
    @NotNull
    private LocalTime jamDatangEnd;
    @NotNull
    private LocalTime jamPulangStart;
    @NotNull
    private LocalTime jamPulangEnd;
    @NotNull
    private LocalTime jamLemburStart;
    @NotNull
    private LocalTime jamLemburEnd;
    @NotNull
    private LocalTime jamDatangBatas;
    @NotNull
    private LocalTime jamPulangBatas;
    @NotNull
    private Boolean isRamadhan;
    @NotNull
    private List<Integer> hari;
}
