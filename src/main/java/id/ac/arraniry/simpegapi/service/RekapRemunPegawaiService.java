package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapRemunPegawai;
import id.ac.arraniry.simpegapi.entity.SelisihRekapRemunPegawai;

import java.util.List;

public interface RekapRemunPegawaiService {
//    List<RekapRemunPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort);
//    Optional<RekapRemunPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    RekapRemunPegawai findById(String id);
//    void deleteByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun);
    void saveAll(List<RekapRemunPegawai> rekapRemunPegawaiList);
    List<RekapRemunPegawai> findByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun);
    List<RekapRemunPegawai> findByTahunAndBulan(Integer tahun, Integer bulan);
    void deleteByRekapId(String rekapId);
    void deleteSelisihByRekapId(String rekapId);
    void saveAllSelisih(List<SelisihRekapRemunPegawai> selisihRemunPegawaiList);
    List<RekapRemunPegawai> findRemunPegawai(String nip, Integer tahun, Integer bulan);
    List<SelisihRekapRemunPegawai> findSelisihRemunPegawai(String idPegawai, Integer tahun, Integer bulan);
    List<RekapRemunPegawai> findByRekapId(String rekapId);
    List<SelisihRekapRemunPegawai> findByRekapIdSelisih(String rekapId);
    SelisihRekapRemunPegawai findSelisihRemunPegawaiById(String id);
}
