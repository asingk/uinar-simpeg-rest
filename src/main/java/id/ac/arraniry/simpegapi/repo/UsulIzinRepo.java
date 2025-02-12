package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.UsulIzin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface UsulIzinRepo extends MongoRepository<UsulIzin, String> {
    Page<UsulIzin> findByNipAndStatusIn(String nip, Set<Integer> status, Pageable pageable);
}
