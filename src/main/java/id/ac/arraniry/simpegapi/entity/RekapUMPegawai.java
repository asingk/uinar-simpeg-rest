package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class RekapUMPegawai {
    @Id
    private String id;
    private String nip;
    private String nama;
    private Integer tahun;
    private Integer bulan;
    private Integer jumlahHari;
    private String unitGaji;
    private LocalDateTime createdDate;
}
