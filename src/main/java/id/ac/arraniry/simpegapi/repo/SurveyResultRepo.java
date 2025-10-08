package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.SurveyResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SurveyResultRepo extends MongoRepository<SurveyResult, String> {
    default Optional<SurveyResult> findTodayAnswerByNip(String nip) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        return findByNipAndAnsweredAtBetween(nip, startOfToday, startOfTomorrow);
    }
    Optional<SurveyResult> findByNipAndSurveyId(String nip, String surveyId);

    Optional<SurveyResult> findByNipAndAnsweredAtBetween(String nip, LocalDateTime start, LocalDateTime end);
}
