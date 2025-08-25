package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.WfaByTanggal;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WfaByTanggalRepo extends MongoRepository<WfaByTanggal, String> {
    Optional<WfaByTanggal> findByTanggal(LocalDate tanggal);
    List<WfaByTanggal> findByTanggalBetween(LocalDate start, LocalDate end, Sort sort);
}
