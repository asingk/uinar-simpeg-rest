package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Rekap {
    @Id
    private String id;
    private String jenisRekap;
    private Integer tahun;
    private Integer bulan;
    private String unitGajiId;
    private String unitRemunId;
    private String kodeAnakSatker;
    private String namaFile;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private Integer progress;
}
