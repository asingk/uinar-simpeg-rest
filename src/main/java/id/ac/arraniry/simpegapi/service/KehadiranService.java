package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranAddRequest;
import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.dto.SaveResponse;

import java.time.LocalDate;
import java.util.List;

public interface KehadiranService {
    KehadiranVO findByNipAndStatusAndTanggal(String nip, String status, LocalDate tanggal);
    SaveResponse save(KehadiranVO kehadiranVO);
    List<KehadiranVO> findByNipAndTanggal(String nip, LocalDate tanggal);
    KehadiranVO findById(String id);
    void delete(KehadiranVO kehadiranVO);
    SaveResponse add(KehadiranAddRequest request);
    Boolean isLibur(LocalDate tanggal);
}
