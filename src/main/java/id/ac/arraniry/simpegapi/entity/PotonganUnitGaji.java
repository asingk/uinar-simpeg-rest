package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PotonganUnitGaji {
    @Id
    private String id;
    private String unitGajiId;
    private String unitGajiNama;
    private Integer bulan;
    private Integer tahun;
    private String nip;
    private String nama;
    private BigDecimal gajiBersih;
    private List<PotonganUnitGajiItem> potongan;
    private BigDecimal jumlahPotongan;
    private BigDecimal thp;
    private String createdBy;
    private LocalDateTime createdDate;
}
