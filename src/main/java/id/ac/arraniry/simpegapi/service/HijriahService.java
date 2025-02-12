package id.ac.arraniry.simpegapi.service;

import java.time.LocalDate;

public interface HijriahService {
    boolean isRamadhan(Integer tahun, LocalDate tanggal);
}
