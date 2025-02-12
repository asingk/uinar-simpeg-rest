package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Izin;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IzinRepo extends MongoRepository<Izin, String> {
    List<Izin> findByNipAndTanggalBetween(String nip, LocalDate start, LocalDate end, Sort sort);
    Optional<Izin> findByNipAndTanggal(String nip, LocalDate tanggal);
}
