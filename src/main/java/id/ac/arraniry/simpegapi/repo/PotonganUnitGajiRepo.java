package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PotonganUnitGajiRepo extends MongoRepository<PotonganUnitGaji, String> {
    void deleteByBulanAndTahunAndUnitGajiId(Integer bulan, Integer tahun, String unitGajiId);
    List<PotonganUnitGaji> findByNipAndTahun(String nip, Integer tahun, Sort sort);
}
