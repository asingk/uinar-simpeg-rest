package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class WfaByTanggal {
    private String id;
    private LocalDate tanggal;
    private String dateString;

    public WfaByTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }
}
