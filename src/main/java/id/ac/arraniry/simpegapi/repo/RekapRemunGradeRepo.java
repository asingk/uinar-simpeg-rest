package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.RekapRemunGrade;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RekapRemunGradeRepo extends MongoRepository<RekapRemunGrade, String> {
    void deleteByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun);
    List<RekapRemunGrade> findByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun, Sort sort);
    List<RekapRemunGrade> findByTahunAndBulan(Integer tahun, Integer bulan, Sort sort);
}
