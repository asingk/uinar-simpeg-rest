package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.dto.SaveResponse;
import id.ac.arraniry.simpegapi.entity.Kehadiran;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KehadiranService {
    Optional<Kehadiran> findByNipAndStatusAndTanggal(String nip, String status, LocalDate tanggal);
    SaveResponse save(KehadiranVO kehadiranVO);
    List<KehadiranVO> findByNipAndTanggal(String nip, LocalDate tanggal);
}
