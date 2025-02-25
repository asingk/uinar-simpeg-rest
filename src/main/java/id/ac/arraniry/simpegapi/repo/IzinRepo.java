package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Izin;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IzinRepo extends MongoRepository<Izin, String> {
    @Query("{'nip' : ?0, 'tanggal' : { $gte : ?1, $lte : ?2 } }")
    List<Izin> findByNipAndTanggalBetween(String nip, LocalDate start, LocalDate end, Sort sort);
    Optional<Izin> findByNipAndTanggal(String nip, LocalDate tanggal);
}
