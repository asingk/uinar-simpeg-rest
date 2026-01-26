package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.JamKerjaHarian;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JamKerjaHarianRepo extends MongoRepository<JamKerjaHarian, String> {
}
