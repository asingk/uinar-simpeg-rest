package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UangMakanCreateRequest {
    @NotNull
    private Integer tahun;
    @NotNull
    private Integer bulan;
    private String unitGaji;
    private String unitRemun;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String jenisRekap;
}
