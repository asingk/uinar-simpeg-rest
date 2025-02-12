package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UangMakanPegawaiVO {
    private String nip;
    private String nama;
    private Integer tahun;
    private Integer bulan;
    private Integer jumlahHari;
    private Integer rupiahHarian;
    private Integer rupiahBulanan;
    private Integer persenPajak;
    private Integer rupiahPajakBulanan;
    private Integer thp;
    private LocalDateTime createdDate;
}
