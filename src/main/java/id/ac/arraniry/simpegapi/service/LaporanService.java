package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.LaporanKehadiranVO;

import java.util.List;

public interface LaporanService {
    List<LaporanKehadiranVO> findLaporanBulananByNip(String nip, int bulan, int tahun);
}
