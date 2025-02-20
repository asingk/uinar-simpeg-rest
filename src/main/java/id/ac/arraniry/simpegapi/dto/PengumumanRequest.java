package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PengumumanRequest {
    @NotNull
    private Boolean isActive;
    @NotBlank
    private String message;
    @NotBlank
    private String nama;
    @NotEmpty
    private List<Integer> statusPegawaiId;
    @NotEmpty
    private List<String> jenisJabatan;
}
