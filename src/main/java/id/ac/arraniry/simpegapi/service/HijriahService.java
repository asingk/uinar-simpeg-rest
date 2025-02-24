package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Hijriah;

import java.time.LocalDate;
import java.util.List;

public interface HijriahService {
    boolean isRamadhan(Integer tahun, LocalDate tanggal);
    List<Hijriah> findAll();
    Hijriah findById(String id);
    String create(Integer tahun, LocalDate awalRamadhan, LocalDate awalSyawal);
    void update(String id, Integer tahun, LocalDate awalRamadhan, LocalDate awalSyawal);
    void delete(String id);
}
