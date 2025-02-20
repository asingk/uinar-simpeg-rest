package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JabBulUpdateRequest {
    private String golongan;
    @NotBlank
    private String jenisJabatan;
    @NotBlank
    private String unitGaji;
    @NotBlank
    private String unitRemun;
    @NotNull
    private Integer remunGrade;
    @NotBlank
    private String grade;
    @NotNull
    private Integer implementasiRemun;
    @NotNull
    private Integer pajak;
    @NotNull
    private Integer uangMakanHarian;
    @NotBlank
    private String admin;
}
