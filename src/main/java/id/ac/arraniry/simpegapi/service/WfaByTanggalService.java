package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByTanggal;

import java.time.LocalDate;
import java.util.Optional;

public interface WfaByTanggalService {
    Optional<WfaByTanggal> findByTanggal(LocalDate tanggal);
}
