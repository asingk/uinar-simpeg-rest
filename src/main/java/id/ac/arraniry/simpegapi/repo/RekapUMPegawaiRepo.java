package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.RekapUMPegawai;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RekapUMPegawaiRepo extends MongoRepository<RekapUMPegawai, String> {
    List<RekapUMPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    Optional<RekapUMPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji);
    List<RekapUMPegawai> findByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji, Sort sort);
    List<RekapUMPegawai> findByTahunAndBulan(Integer tahun, Integer bulan, Sort sort);
}
