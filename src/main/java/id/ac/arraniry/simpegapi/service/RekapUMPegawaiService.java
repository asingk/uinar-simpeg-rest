package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapUMPegawai;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface RekapUMPegawaiService {
    List<RekapUMPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    Optional<RekapUMPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    Optional<RekapUMPegawai> findById(String id);
    void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji);
    void saveAll(List<RekapUMPegawai> rekapPegawais);
    List<RekapUMPegawai> findByTahunAndBulanAndStatusPegawaiAndUnitGaji(Integer tahun, Integer bulan, String statusPegawai, String unitGaji);
    List<RekapUMPegawai> findByTahunAndBulanAndStatusPegawai(Integer tahun, Integer bulan, String statusPegawai);

}
