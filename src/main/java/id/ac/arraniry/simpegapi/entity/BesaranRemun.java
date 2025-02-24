package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class BesaranRemun {
    @Id
    private String id;
    private Integer statusPegawaiId;
    private String statusPegawaiNama;
    private String jenisJabatan;
    private Integer persen;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
