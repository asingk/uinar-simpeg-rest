package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;

import java.time.LocalDate;
import java.util.List;

public interface HariLiburTapiKerjaService {
    List<HariLiburTapiKerja> findAll();
    List<HariLiburTapiKerja> findByTahun(Integer tahun);
    String create(LocalDate tanggal);
    void delete(String id);
}
