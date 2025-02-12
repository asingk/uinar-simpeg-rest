package id.ac.arraniry.simpegapi.dto;

import id.ac.arraniry.simpegapi.entity.Kehadiran;
import id.ac.arraniry.simpegapi.entity.KehadiranArc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveResponse {
    private String id;
    private LocalDateTime waktu;
    private String status;

    public SaveResponse(Kehadiran kehadiran) {
        this.id = kehadiran.getId();
        this.waktu = kehadiran.getWaktu();
        this.status = kehadiran.getStatus();
    }

    public SaveResponse(KehadiranArc kehadiran) {
        this.id = kehadiran.getId();
        this.waktu = kehadiran.getWaktu();
        this.status = kehadiran.getStatus();
    }
}
