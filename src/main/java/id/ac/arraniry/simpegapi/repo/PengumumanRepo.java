package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Pengumuman;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PengumumanRepo extends MongoRepository<Pengumuman, String> {
    Optional<Pengumuman> findByIsActive(Boolean isActive);
}
