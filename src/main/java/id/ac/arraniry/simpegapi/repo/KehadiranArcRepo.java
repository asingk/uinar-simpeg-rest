package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.KehadiranArc;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface KehadiranArcRepo extends MongoRepository<KehadiranArc, String> {

    @Query("{ 'pegawai.nip' : ?0, 'waktu' : { $gte : ?1, $lte : ?2 } }")
    List<KehadiranArc> findByNipAndTanggalBetween(String nip, LocalDateTime start, LocalDateTime end, Sort sort);

    List<KehadiranArc> findByPegawaiNipAndTanggal(String nip, String tanggal);
}
