package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface HariLiburTapiKerjaRepo extends MongoRepository<HariLiburTapiKerja, String> {

    @Query("{ 'tanggal' : { $gte : ?0, $lte : ?1 } }")
    List<HariLiburTapiKerja> findByTanggalBetween(LocalDate start, LocalDate end, Sort sort);

}
