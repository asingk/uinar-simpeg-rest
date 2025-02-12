package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsulIzinEditRequest {
    @NotNull
    private Integer status;
    private String ket;
    @NotBlank
    private String updatedBy;
}
