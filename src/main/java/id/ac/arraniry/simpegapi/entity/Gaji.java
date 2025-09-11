package id.ac.arraniry.simpegapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
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
    private BigDecimal gajiPokok;
    private BigDecimal tunjanganIstri;
    private BigDecimal tunjanganAnak;
    private BigDecimal tunjanganUmum;
    private BigDecimal tunjanganStruktural;
    private BigDecimal tunjanganFungsional;
    private BigDecimal pembulatan;
    private BigDecimal tunjanganBeras;
    private BigDecimal tunjanganPajak;
    private BigDecimal iwp;
    private BigDecimal pph;
    private BigDecimal netto;
    private BigDecimal bpjs;
    private String createdBy;
    private LocalDateTime createdDate;
}
