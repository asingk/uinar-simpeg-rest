package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Survey;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SurveyRepo extends MongoRepository<Survey, String> {
    default Optional<Survey> findToday() {
        return findByTanggal(LocalDate.now());
    }
    Optional<Survey> findByTanggal(LocalDate tanggal);
}
