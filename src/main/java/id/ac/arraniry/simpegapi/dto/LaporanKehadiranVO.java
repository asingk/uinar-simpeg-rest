package id.ac.arraniry.simpegapi.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LaporanKehadiranVO {

    private LocalDate tanggal;
    private String hari;
    private String jadwalDatang; // Format "HH:mm"
    private String absenDatang;  // Format "HH:mm"
    private String jadwalPulang; // Format "HH:mm"
    private String absenPulang;  // Format "HH:mm"
    private Integer cepatTelatDatang; // Selisih dalam menit
    private Integer cepatTelatPulang; // Selisih dalam menit
    private String keterangan;

}
