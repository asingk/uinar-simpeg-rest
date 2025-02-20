package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapRemunGrade;

import java.util.List;

public interface RekapRemunGradeService {
    void deleteByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun);
    void saveAll(List<RekapRemunGrade> rekapRemunGradeList);
    List<RekapRemunGrade> findByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun);
    List<RekapRemunGrade> findByTahunAndBulan(Integer tahun, Integer bulan);
}
