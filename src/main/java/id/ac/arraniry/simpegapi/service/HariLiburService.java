package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLibur;

import java.time.LocalDate;
import java.util.List;

public interface HariLiburService {
    List<HariLibur> findAll();
    List<LocalDate> hariLiburBulanan(int tahun, int bulan);
    List<HariLibur> findByTahun(Integer tahun);
}
