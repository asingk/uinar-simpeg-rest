package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapRemunGrade;
import id.ac.arraniry.simpegapi.repo.RekapRemunGradeRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RekapRemunGradeServiceImpl implements RekapRemunGradeService {

    private final RekapRemunGradeRepo rekapRemunGradeRepo;

    public RekapRemunGradeServiceImpl(RekapRemunGradeRepo rekapRemunGradeRepo) {
        this.rekapRemunGradeRepo = rekapRemunGradeRepo;
    }

    @Override
    public void deleteByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun) {
        rekapRemunGradeRepo.deleteByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun);
    }

    @Override
    public void saveAll(List<RekapRemunGrade> rekapRemunGradeList) {
        rekapRemunGradeRepo.saveAll(rekapRemunGradeList);
    }

    @Override
    public List<RekapRemunGrade> findByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun) {
        return rekapRemunGradeRepo.findByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun, Sort.by(Sort.Direction.DESC, "jumlahNetto"));
    }

    @Override
    public List<RekapRemunGrade> findByTahunAndBulan(Integer tahun, Integer bulan) {
        return rekapRemunGradeRepo.findByTahunAndBulan(tahun, bulan, Sort.by(Sort.Direction.DESC, "jumlahNetto"));
    }

}
