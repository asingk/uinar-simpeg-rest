package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.HariLibur;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface HariLiburRepo extends MongoRepository<HariLibur, String> {

    @Query("{ 'tanggal' : { $gte : ?0, $lte : ?1 } }")
    List<HariLibur> findByTanggalBetween(LocalDate start, LocalDate end, Sort sort);

}
