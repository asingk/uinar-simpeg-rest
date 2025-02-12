package id.ac.arraniry.simpegapi.repo;


import id.ac.arraniry.simpegapi.entity.Hijriah;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HijriahRepo extends MongoRepository<Hijriah, String> {
    Optional<Hijriah> findByTahun(Integer tahun);
}
