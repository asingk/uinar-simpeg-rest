package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class RekapRemunGrade {
    @Id
    private String id;
    private String grade;
    private Integer jumlahPenerima;
    private Integer implementasiRemunPersen;
    private Integer implementasiRemun;
    private Integer remunP1;
    private Integer jumlahRemunP1;
    private Integer jumlahPotongan;
    private Integer jumlahSetelahPotongan;
    private Integer jumlahPajak;
    private Integer jumlahNetto;
    private Integer tahun;
    private Integer bulan;
    private String unitRemun;
}
