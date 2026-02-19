package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.JamKerjaHarian;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JamKerjaHarianRepo extends MongoRepository<JamKerjaHarian, String> {
    Optional<JamKerjaHarian> findByDateString(String dateString);
}
