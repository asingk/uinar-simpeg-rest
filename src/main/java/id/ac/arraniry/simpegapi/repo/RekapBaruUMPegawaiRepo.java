package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.RekapBaruUMPegawai;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RekapBaruUMPegawaiRepo extends MongoRepository<RekapBaruUMPegawai, String> {
    void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji);
    List<RekapBaruUMPegawai> findByTahunAndBulanAndStatusPegawaiAndUnitGaji(Integer tahun, Integer bulan, String statusPegawai, String unitGaji, Sort sort);
    List<RekapBaruUMPegawai> findByTahunAndBulanAndStatusPegawai(Integer tahun, Integer bulan, String statusPegawai, Sort sort);
}
