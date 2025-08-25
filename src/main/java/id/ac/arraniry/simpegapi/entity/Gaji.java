package id.ac.arraniry.simpegapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gaji {
    @Id
    private String id;
    private String kodeAnakSatker;
    private Integer bulan;
    private Integer tahun;
    private String nip;
    private String nama;
    private Integer gajiPokok;
    private Integer tunjanganIstri;
    private Integer tunjanganAnak;
    private Integer tunjanganUmum;
    private Integer tunjanganStruktural;
    private Integer tunjanganFungsional;
    private Integer pembulatan;
    private Integer tunjanganBeras;
    private Integer tunjanganPajak;
    private Integer iwp;
    private Integer pph;
    private Integer netto;
    private Integer bpjs;
    private String createdBy;
    private LocalDateTime createdDate;
}
