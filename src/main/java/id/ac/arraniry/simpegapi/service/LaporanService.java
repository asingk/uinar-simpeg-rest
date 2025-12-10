package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.dto.LaporanKehadiranVO;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.entity.RekapUMPegawai;

import java.util.List;

public interface LaporanService {
    List<LaporanKehadiranVO> findLaporanBulananByNip(String nip, int bulan, int tahun);
    List<KehadiranVO> findLaporanUangMakanBaru(Integer bulan, Integer tahun, List<JabatanBulanan> jabatanBulanans);
    List<RekapUMPegawai> generateUangMakanFormatLama(List<KehadiranVO> kehadiranVOList, List<JabatanBulanan> jabatanBulananList);
//    List<RekapRemunPegawai> findLaporanRemon(Integer bulan, Integer tahun, List<JabatanBulanan> jabatanBulanans);
//    List<RekapRemunGrade> findRekapRemonGrade(Integer bulan, Integer tahun, String unitRemun, List<RekapRemunPegawai> rekapRemunPegawaiList);

}
