package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PotonganUnitGaji {
    @Id
    private String id;
    private String kodeAnakSatker;
    private Integer bulan;
    private Integer tahun;
    private String nip;
    private String nama;
    private BigDecimal gajiBersih;
    private BigDecimal pinjaman;
    private BigDecimal pengembalian;
    private BigDecimal dw;
    private BigDecimal korpri;
    private BigDecimal masjid;
    private BigDecimal sosialUIN;
    private BigDecimal zakat;
    private BigDecimal infaq;
    private BigDecimal bsm;
    private BigDecimal hikmahWakilah;
    private BigDecimal jumlahPotongan;
    private BigDecimal thp;
    private String createdBy;
    private LocalDateTime createdDate;
}
