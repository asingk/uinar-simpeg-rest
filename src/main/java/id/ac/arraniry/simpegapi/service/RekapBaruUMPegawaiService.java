package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.entity.RekapBaruUMPegawai;

import java.util.List;

public interface RekapBaruUMPegawaiService {
    void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji);
    void saveAll(List<KehadiranVO> kehadiranVOList, List<JabatanBulanan> jabatanBulanansUmAsn);
    List<RekapBaruUMPegawai> findByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji);
    List<RekapBaruUMPegawai> findByTahunAndBulan(Integer tahun, Integer bulan);
}
