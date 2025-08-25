package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByTanggal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WfaByTanggalService {
    Optional<WfaByTanggal> findByTanggal(LocalDate tanggal);
    List<WfaByTanggal> findByTahun(Integer tahun);
    String create(LocalDate tanggal);
    void deleteById(String id);
}
