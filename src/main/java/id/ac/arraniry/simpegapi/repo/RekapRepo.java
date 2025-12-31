package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Rekap;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RekapRepo extends MongoRepository<Rekap, String> {
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitGajiId(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId);
    List<Rekap> findByJenisRekapAndTahun(String jenisRekap, Integer tahun, Sort sort);
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndKodeAnakSatker(String jenisRekap, Integer tahun, Integer bulan, String kodeAnakSatker);
    List<Rekap> findByJenisRekapAndTahunAndKodeAnakSatker(String jenisRekap, Integer tahun, String kodeAnakSatker, Sort sort);
    List<Rekap> findByJenisRekapAndTahunAndUnitGajiId(String jenisRekap, Integer tahun, String unitGajiId, Sort sort);
    List<Rekap> findByJenisRekapAndTahunAndUnitRemunId(String jenisRekap, Integer tahun, String unitRemunId, Sort sort);
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitRemunId(String jenisRekap, Integer tahun, Integer bulan, String unitRemunId);
    void deleteByJenisRekapAndTahunAndBulanAndKodeAnakSatker(String jenisRekap, Integer tahun, Integer bulan, String kodeAnakSatker);
    void deleteByJenisRekapAndTahunAndBulanAndUnitGajiId(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId);
}
