package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PotonganUnitGajiRepo extends MongoRepository<PotonganUnitGaji, String> {
    void deleteByBulanAndTahunAndKodeAnakSatker(Integer bulan, Integer tahun, String kodeAnakSatker);
}
