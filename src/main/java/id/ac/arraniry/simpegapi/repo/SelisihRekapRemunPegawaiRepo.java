package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.SelisihRekapRemunPegawai;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SelisihRekapRemunPegawaiRepo extends MongoRepository<SelisihRekapRemunPegawai, String> {
    List<SelisihRekapRemunPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    Optional<SelisihRekapRemunPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    List<SelisihRekapRemunPegawai> findByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun, Sort sort);
    List<SelisihRekapRemunPegawai> findByTahunAndBulan(Integer tahun, Integer bulan, Sort sort);
    void deleteByRekapId(String rekapId);
    List<SelisihRekapRemunPegawai> findByRekapId(String rekapId);
}
