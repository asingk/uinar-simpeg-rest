package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class RekapRemunPegawai {
    @Id
    private String id;
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
    private Double persenPotongan;
    private String unitRemun;
    private LocalDateTime createdDate;
}
