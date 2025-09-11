package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.RekapBaruUMPegawai;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RekapBaruUMPegawaiRepo extends MongoRepository<RekapBaruUMPegawai, String> {
    void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji);
    List<RekapBaruUMPegawai> findByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji, Sort sort);
    List<RekapBaruUMPegawai> findByTahunAndBulan(Integer tahun, Integer bulan, Sort sort);
}
