package id.ac.arraniry.simpegapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hijriah {
    private String id;
    private Integer tahun;
    private LocalDate awalRamadhan;
    private LocalDate awalSyawal;

    public Hijriah(Integer tahun, LocalDate awalRamadhan, LocalDate awalSyawal) {
        this.tahun = tahun;
        this.awalRamadhan = awalRamadhan;
        this.awalSyawal = awalSyawal;
    }
}
