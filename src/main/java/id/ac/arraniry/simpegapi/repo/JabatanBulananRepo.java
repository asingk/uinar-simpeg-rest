package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JabatanBulananRepo extends MongoRepository<JabatanBulanan, String> {
    Optional<JabatanBulanan> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    List<JabatanBulanan> findByNipAndTahun(String nip, Integer tahun, Sort sort);
}
