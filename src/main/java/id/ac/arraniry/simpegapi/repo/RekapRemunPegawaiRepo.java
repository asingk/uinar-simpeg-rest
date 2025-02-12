package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.RekapRemunPegawai;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RekapRemunPegawaiRepo extends MongoRepository<RekapRemunPegawai, String> {
    List<RekapRemunPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    Optional<RekapRemunPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
}
