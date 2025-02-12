package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RemunPegawaiVO {
    private String nip;
    private String nama;
    private Integer tahun;
    private Integer bulan;
    private Integer d1;
    private Integer d2;
    private Integer d3;
    private Integer d4;
    private Integer p1;
    private Integer p2;
    private Integer p3;
    private Integer p4;
    private String grade;
    private Integer remunGrade;
    private Integer implementasiRemunPersen;
    private Integer implementasiRemun;
    private Integer remunP1;
    private Double persenPotongan;
    private Integer rupiahPotongan;
    private Integer setelahPotongan;
    private Integer persenPajak;
    private Integer rupiahPajak;
    private Integer netto;
    private LocalDateTime createdDate;
}
