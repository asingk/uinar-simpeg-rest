package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UangMakanRequest {
    @NotNull
    private Integer jumlah;
    @NotBlank
    private String updatedBy;
}
