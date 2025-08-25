package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Gaji;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GajiRepo extends MongoRepository<Gaji, String> {
    void deleteByBulanAndTahunAndKodeAnakSatker(Integer bulan, Integer tahun, String kodeAnakSatker);
    List<Gaji> findByNipAndTahun(String nip, Integer tahun, Sort sort);
}
