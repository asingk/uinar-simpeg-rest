package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Pajak;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PajakRepo extends MongoRepository<Pajak, String> {
}
