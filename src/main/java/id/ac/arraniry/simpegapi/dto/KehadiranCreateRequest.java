package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KehadiranCreateRequest {
    @NotBlank
    private String idPegawai;
    private Double longitude;
    private Double latitude;
}
