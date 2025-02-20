package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JabatanBulananRepo extends MongoRepository<JabatanBulanan, String> {
    Optional<JabatanBulanan> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    List<JabatanBulanan> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    List<JabatanBulanan> findByUnitGajiAndTahunAndBulanAndIdStatusPegawaiIn(String unitGaji, Integer tahun, Integer bulan, List<Integer> idStatusPegList, Sort sort);
    @Query("{ 'unitRemun': ?0, 'tahun': ?1, 'bulan': ?2, 'jenisJabatan': { $in: ['Tendik', 'DT'] } }")
    List<JabatanBulanan> findRemunByUnitRemunAndTahunAndBulan(String unitRemun, Integer tahun, Integer bulan, Sort sort);
}
