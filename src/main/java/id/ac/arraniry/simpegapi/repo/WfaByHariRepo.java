package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.WfaByHari;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WfaByHariRepo extends MongoRepository<WfaByHari, String> {
    Optional<WfaByHari> findByHari(Integer hari);
}
