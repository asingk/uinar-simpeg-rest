package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.JenisJabatan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JenisJabatanRepo extends MongoRepository<JenisJabatan, String> {
}
