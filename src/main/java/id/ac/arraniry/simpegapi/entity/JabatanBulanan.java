package id.ac.arraniry.simpegapi.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JabatanBulanan {
    private String id;
    private String nip;
    private String nama;
    private String golongan;
    private Integer idStatusPegawai;
    private String namaStatusPegawai;
    private String jenisJabatan;
    private String unitGaji;
    private String unitRemun;
    private Integer remunGrade;
    private String grade;
    private String jabatan;
    private Integer tahun;
    private Integer bulan;
    private Integer implementasiRemun;
    private Integer pajak;
    private Integer uangMakanHarian;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
