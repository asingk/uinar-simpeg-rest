package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;

import java.util.List;

public interface HariLiburTapiKerjaService {
    List<HariLiburTapiKerja> findAll();
    List<HariLiburTapiKerja> findByTahun(Integer tahun);
}
