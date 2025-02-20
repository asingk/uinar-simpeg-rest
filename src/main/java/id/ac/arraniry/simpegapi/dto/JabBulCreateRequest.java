package id.ac.arraniry.simpegapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JabBulCreateRequest {
    @NotBlank
    private String namaPegawai;
    @NotBlank
    private String golongan;
    @NotNull
    private Integer idStatusPegawai;
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
    private Integer tahun;
    @NotNull
    private Integer bulan;
    @NotNull
    private Integer implementasiRemun;
    @NotNull
    private Integer pajak;
    @NotNull
    private Integer uangMakanHarian;
    @NotBlank
    private String admin;
}
