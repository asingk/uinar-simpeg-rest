package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RekapBaruUMPegawai {
    @Id
    private String id;
    private String nip;
    private String nama;
    private String tanggal;
    private Integer tahun;
    private Integer bulan;
    private String unitGaji;
    private String statusPegawai;
    private LocalDateTime createdDate;
}
