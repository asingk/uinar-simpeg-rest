package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Rekap;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RekapRepo extends MongoRepository<Rekap, String> {
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId, String unitRemunId);
    List<Rekap> findByJenisRekapAndTahunAndUnitGajiId(String jenisRekap, Integer tahun, String unitGajiId, Sort sort);
    List<Rekap> findByJenisRekapAndTahun(String jenisRekap, Integer tahun, Sort sort);
}
